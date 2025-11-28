/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.sca.services

import play.api.Logging
import play.api.i18n.{Lang, Messages}
import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models._
import uk.gov.hmrc.sca.utils.{Keys, WebchatUtil}
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, StandardScaLayout}

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class WrapperService @Inject() (
  ptaMenuBar: PtaMenuBar,
  newScaLayout: StandardScaLayout,
  webchatUtil: WebchatUtil,
  appConfig: AppConfig
) extends Logging {

  private lazy val defaultBannerConfig: BannerConfig = BannerConfig(
    showAlphaBanner = appConfig.showAlphaBanner,
    showBetaBanner = appConfig.showBetaBanner,
    showHelpImproveBanner = false // deprecated; controlled via wrapper-data ur-banners now
  )

  def standardScaLayout(
    content: HtmlFormat.Appendable,
    pageTitle: Option[String] = None,
    serviceURLs: ServiceURLs,
    serviceNameKey: Option[String] = appConfig.serviceNameKey,
    sidebarContent: Option[Html] = None,
    @deprecated("Please use appConfig for this setting rather than passing it as a parameter.", since = "3.0.0")
    timeOutUrl: Option[String] = appConfig.timeOutUrl,
    @deprecated("Please use appConfig for this setting rather than passing it as a parameter.", since = "3.0.0")
    keepAliveUrl: String = appConfig.keepAliveUrl,
    showBackLinkJS: Boolean = false,
    backLinkUrl: Option[String] = None,
    scripts: Seq[HtmlFormat.Appendable] = Seq.empty,
    styleSheets: Seq[HtmlFormat.Appendable] = Seq.empty,
    bannerConfig: BannerConfig = defaultBannerConfig,
    optTrustedHelper: Option[TrustedHelper] = None,
    fullWidth: Boolean = true,
    hideMenuBar: Boolean = false,
    disableSessionExpired: Boolean = appConfig.disableSessionExpired
  )(implicit messages: Messages, requestHeader: RequestHeader): HtmlFormat.Appendable = {

    val showSignOutInHeader = (serviceURLs.signOutUrl, hideMenuBar) match {
      case (Some(_), false) => false
      case (Some(_), true)  => true // if the menu is hidden the user must be unauthenticated
      case (None, false)    => throw new RuntimeException("The PTA menu cannot be shown without a signout url")
      case (None, true)     => false
    }

    val bespokeBannerFromWrapper: Option[BespokeUserResearchBanner] =
      getBespokeUserResearchBannerForPage

    newScaLayout(
      menu = if (hideMenuBar) None else Some(ptaMenuBar(sortMenuItemConfig(serviceURLs.signOutUrl))),
      serviceURLs = serviceURLs,
      serviceNameKey = serviceNameKey,
      pageTitle = pageTitle,
      sidebarContent = sidebarContent,
      timeOutUrl = timeOutUrl,
      keepAliveUrl = keepAliveUrl,
      showBackLinkJS = showBackLinkJS,
      backLinkUrl = backLinkUrl,
      showSignOutInHeader = showSignOutInHeader,
      scripts = scripts ++ webchatUtil.getWebchatScripts,
      styleSheets = styleSheets,
      bannerConfig = bannerConfig,
      fullWidth = fullWidth,
      disableSessionExpired = disableSessionExpired,
      optTrustedHelper = optTrustedHelper,
      urBannerUrl = if (urBannerEnabled()) getUrBannerUrl else None,
      bespokeUserResearchBanner = bespokeBannerFromWrapper
    )(content)
  }

  @deprecated(
    "Inline instead. This will be removed in a future release.",
    since = "1.7.0"
  )
  def safeSignoutUrl(continueUrl: Option[RedirectUrl] = None): Option[String] = continueUrl match {
    case Some(continue) if continue.getEither(OnlyRelative).isRight =>
      Some(continue.getEither(OnlyRelative).toOption.get.url)
    case _                                                          =>
      appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))
  }

  private def sortMenuItemConfig(signoutUrl: Option[String])(implicit requestHeader: RequestHeader): PtaMenuConfig = {
    implicit val lang: Lang = Lang(requestHeader.cookies.get("PLAY_LANG").map(_.value).getOrElse("en"))

    val wrapperDataResponse =
      getWrapperDataResponse(requestHeader).getOrElse(appConfig.fallbackWrapperDataResponse)
    val unreadMessageCount  = getMessageDataFromRequest(requestHeader)

    if (requestHeader.attrs.get(Keys.wrapperFilterHasRun).isEmpty) {
      logger.error(
        s"[SCA Wrapper Library][WrapperService][sortMenuItemConfig]{Expecting Wrapper Data in " +
          s"the request but none was there due to missing/ misconfigured wrapper data filter}"
      )
    }

    if (requestHeader.attrs.get(Keys.wrapperIsAuthenticatedKey).isEmpty) {
      logger.warn(
        s"[SCA Wrapper Library][WrapperService][sortMenuItemConfig]{The user is not authenticated, the menu should not be used. The default menu was returned}"
      )
    }

    val menuItemConfigWithSignout            = setSignoutUrl(signoutUrl, wrapperDataResponse.menuItemConfig)
    val menuItemConfigWithUnreadMessageCount = setUnreadMessageCount(unreadMessageCount, menuItemConfigWithSignout)

    PtaMenuConfig(
      leftAlignedItems = menuItemConfigWithUnreadMessageCount.filter(_.leftAligned).sortBy(_.position),
      rightAlignedItems = menuItemConfigWithUnreadMessageCount.filterNot(_.leftAligned).sortBy(_.position),
      ptaMinMenuConfig = wrapperDataResponse.ptaMinMenuConfig
    )
  }

  private def setSignoutUrl(signoutUrl: Option[String], menuItemConfig: Seq[MenuItemConfig]): Seq[MenuItemConfig] =
    menuItemConfig.flatMap {
      case signout if signout.id == "signout" => signoutUrl.map(url => signout.copy(href = url))
      case other                              => Some(other)
    }

  private def setUnreadMessageCount(
    unreadMessageCount: Option[Int],
    menuItemConfig: Seq[MenuItemConfig]
  ): Seq[MenuItemConfig] =
    Try {
      menuItemConfig.find(_.id == "messages").fold(menuItemConfig) { messageMenuItemConfig =>
        menuItemConfig.updated(
          menuItemConfig.indexWhere(_.id == "messages"),
          messageMenuItemConfig.copy(notificationBadge = unreadMessageCount)
        )
      }
    } match {
      case Success(config)    => config
      case Failure(exception) =>
        logger.error(
          s"[SCA Wrapper Library][WrapperService][setUnreadMessageCount] Set unread message count exception: ${exception.getMessage}"
        )
        menuItemConfig
    }

  private def getWrapperDataResponse(requestHeader: RequestHeader): Option[WrapperDataResponse] =
    requestHeader.attrs.get(Keys.wrapperDataKey)

  private def getMessageDataFromRequest(requestHeader: RequestHeader): Option[Int] =
    requestHeader.attrs.get(Keys.messageDataKey)

  private def getUrBannerDetailsForPage(implicit requestHeader: RequestHeader): Option[UrBanner] = {
    val wrapperDataResponse = getWrapperDataResponse(requestHeader)
    wrapperDataResponse.flatMap { response =>
      response.urBanners.find(_.page.equals(requestHeader.uri))
    }
  }

  private def getUrBannerUrl(implicit requestHeader: RequestHeader): Option[String] =
    getUrBannerDetailsForPage match {
      case Some(urBanner) => Some(urBanner.link)
      case None           => appConfig.helpImproveBannerUrl
    }

  private def urBannerEnabled()(implicit requestHeader: RequestHeader): Boolean =
    getUrBannerDetailsForPage.exists(_.isEnabled)

  private def getBespokeUserResearchBannerForPage(implicit
    requestHeader: RequestHeader
  ): Option[BespokeUserResearchBanner] =
    getUrBannerDetailsForPage.flatMap { urBanner =>
      if (urBanner.isBespoke) {
        for {
          titleEn    <- urBanner.titleEn
          titleCy    <- urBanner.titleCy
          linkTextEn <- urBanner.linkTextEn
          linkTextCy <- urBanner.linkTextCy
        } yield BespokeUserResearchBanner(
          url = urBanner.link,
          titleEn = titleEn,
          titleCy = titleCy,
          linkTextEn = linkTextEn,
          linkTextCy = linkTextCy,
          hideCloseButton = urBanner.hideCloseButton.getOrElse(false)
        )
      } else {
        None
      }
    }
}
