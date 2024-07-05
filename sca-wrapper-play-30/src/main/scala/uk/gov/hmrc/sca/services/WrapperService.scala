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
import play.api.mvc.Request
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.{BannerConfig, MenuItemConfig, PtaMenuConfig, UrBanner, WrapperDataResponse}
import uk.gov.hmrc.sca.utils.Keys
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, ScaLayout, StandardScaLayout}

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class WrapperService @Inject() (
  ptaMenuBar: PtaMenuBar,
  scaLayout: ScaLayout,
  newScaLayout: StandardScaLayout,
  appConfig: AppConfig
) extends Logging {

  lazy val defaultBannerConfig: BannerConfig = BannerConfig(
    showAlphaBanner = appConfig.showAlphaBanner,
    showBetaBanner = appConfig.showBetaBanner,
    showHelpImproveBanner = appConfig.showHelpImproveBanner
  )

  @deprecated(
    "Use standardScaLayout method instead - this is support the HMRCStandardPage template instead of deprecated HmrcLayout",
    since = "1.0.48"
  )
  def layout(
    content: HtmlFormat.Appendable,
    pageTitle: Option[String] = None,
    serviceNameKey: Option[String] = appConfig.serviceNameKey,
    serviceNameUrl: Option[String] = None,
    sidebarContent: Option[Html] = None,
    signoutUrl: Option[String] = None,
    timeOutUrl: Option[String] = appConfig.timeOutUrl,
    keepAliveUrl: String = appConfig.keepAliveUrl,
    showBackLinkJS: Boolean = false,
    backLinkUrl: Option[String] = None,
    scripts: Seq[HtmlFormat.Appendable] = Seq.empty,
    styleSheets: Seq[HtmlFormat.Appendable] = Seq.empty,
    bannerConfig: BannerConfig = defaultBannerConfig,
    optTrustedHelper: Option[TrustedHelper] = None,
    fullWidth: Boolean = true,
    hideMenuBar: Boolean = false,
    disableSessionExpired: Boolean = appConfig.disableSessionExpired,
    accessibilityStatementUrl: Option[String] = None
  )(implicit messages: Messages, hc: HeaderCarrier, request: Request[_]): HtmlFormat.Appendable = {
    val showSignOutInHeader = (signoutUrl, hideMenuBar) match {
      case (Some(_), false) => false
      case (Some(_), true)  => true // should we throw exception? if the menu is hidden the user must be unauthenticated
      case (None, false)    => throw new RuntimeException("The PTA menu cannot be shown without a signout url")
      case (None, true)     => false
    }
    scaLayout(
      menu = ptaMenuBar(sortMenuItemConfig(signoutUrl)),
      serviceNameKey = serviceNameKey,
      serviceNameUrl = serviceNameUrl,
      pageTitle = pageTitle,
      sidebarContent = sidebarContent,
      signoutUrl = signoutUrl,
      timeOutUrl = timeOutUrl,
      keepAliveUrl = keepAliveUrl,
      showBackLinkJS = showBackLinkJS,
      backLinkUrl = backLinkUrl,
      showSignOutInHeader = showSignOutInHeader,
      scripts = scripts,
      styleSheets = styleSheets,
      bannerConfig = bannerConfig,
      fullWidth = fullWidth,
      hideMenuBar = hideMenuBar,
      disableSessionExpired = disableSessionExpired,
      optTrustedHelper = optTrustedHelper,
      accessibilityStatementUrl = accessibilityStatementUrl
    )(content)
  }

  def standardScaLayout(
    content: HtmlFormat.Appendable,
    pageTitle: Option[String] = None,
    serviceURLs: ServiceURLs,
    serviceNameKey: Option[String] = appConfig.serviceNameKey,
    sidebarContent: Option[Html] = None,
    timeOutUrl: Option[String] = appConfig.timeOutUrl,
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
  )(implicit messages: Messages, request: Request[_]): HtmlFormat.Appendable = {
    val showSignOutInHeader = (serviceURLs.signOutUrl, hideMenuBar) match {
      case (Some(_), false) => false
      case (Some(_), true)  => true // should we throw exception? if the menu is hidden the user must be unauthenticated
      case (None, false)    => throw new RuntimeException("The PTA menu cannot be shown without a signout url")
      case (None, true)     => false
    }

    newScaLayout(
      menu = ptaMenuBar(sortMenuItemConfig(serviceURLs.signOutUrl)),
      serviceURLs = serviceURLs,
      serviceNameKey = serviceNameKey,
      pageTitle = pageTitle,
      sidebarContent = sidebarContent,
      timeOutUrl = timeOutUrl,
      keepAliveUrl = keepAliveUrl,
      showBackLinkJS = showBackLinkJS,
      backLinkUrl = backLinkUrl,
      showSignOutInHeader = showSignOutInHeader,
      scripts = scripts,
      styleSheets = styleSheets,
      bannerConfig = bannerConfig,
      fullWidth = fullWidth,
      hideMenuBar = hideMenuBar,
      disableSessionExpired = disableSessionExpired,
      optTrustedHelper = optTrustedHelper,
      urBannerUrl = if (urBannerEnabled(bannerConfig)) getUrBannerUrl else None
    )(content)
  }

  @deprecated(
    "Inline instead. This will be removed in a future release.",
    since = "1.7.0"
  )
  def safeSignoutUrl(continueUrl: Option[RedirectUrl] = None): Option[String] = continueUrl match {
    case Some(continue) if continue.getEither(OnlyRelative).isRight =>
      Some(continue.getEither(OnlyRelative).toOption.get.url)
    case _                                                          => appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))
  }

  private def sortMenuItemConfig(signoutUrl: Option[String])(implicit request: Request[_]): PtaMenuConfig = {
    implicit val lang: Lang = Lang(request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en"))

    val wrapperDataResponse =
      getWrapperDataResponse(request).getOrElse(appConfig.fallbackWrapperDataResponse)
    val unreadMessageCount  = getMessageDataFromRequest(request)

    val menuItemConfigWithSignout            = setSignoutUrl(signoutUrl, wrapperDataResponse.menuItemConfig)
    val menuItemConfigWithUnreadMessageCount = setUnreadMessageCount(unreadMessageCount, menuItemConfigWithSignout)

    PtaMenuConfig(
      leftAlignedItems = menuItemConfigWithUnreadMessageCount.filter(_.leftAligned).sortBy(_.position),
      rightAlignedItems = menuItemConfigWithUnreadMessageCount.filterNot(_.leftAligned).sortBy(_.position),
      ptaMinMenuConfig = wrapperDataResponse.ptaMinMenuConfig
    )
  }

  private def setSignoutUrl(signoutUrl: Option[String], menuItemConfig: Seq[MenuItemConfig]) =
    menuItemConfig.flatMap {
      case signout if signout.id == "signout" => signoutUrl.map(url => signout.copy(href = url))
      case other                              => Some(other)
    }

  private def setUnreadMessageCount(unreadMessageCount: Option[Int], menuItemConfig: Seq[MenuItemConfig]) =
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
          s"[SCA Wrapper Library][WrapperService][setUnreadMessageCount] Set unread message count  exception: ${exception.getMessage}"
        )
        menuItemConfig
    }

  private def getWrapperDataResponse(request: Request[_]): Option[WrapperDataResponse] = {
    val result = request.attrs.get(Keys.wrapperDataKey)
    if (result.isEmpty) {
      logger.warn(
        s"[SCA Wrapper Library][WrapperService][getWrapperDataResponse]{ Expecting Wrapper Data in " +
          s"the request but none was there [${appConfig.serviceUrl}]"
      )
    }
    result
  }

  private def getMessageDataFromRequest(request: Request[_]): Option[Int] = {
    val result = request.attrs.get(Keys.messageDataKey)
    if (result.isEmpty) {
      logger.warn(
        "[SCA Wrapper Library][WrapperService][getMessageDataFromRequest] Expecting Message Data in " +
          "the request but none was there"
      )
    }
    result.flatten
  }

  private def getUrBannerDetailsForPage(implicit request: Request[_]): Option[UrBanner] = {
    val wrapperDataResponse = getWrapperDataResponse(request)
    wrapperDataResponse.flatMap { response =>
      response.urBanners.find(_.page.equals(request.uri))
    }
  }

  private def getUrBannerUrl(implicit request: Request[_]): Option[String] =
    getUrBannerDetailsForPage match {
      case Some(urBanner) => Some(urBanner.link)
      case None           => appConfig.helpImproveBannerUrl
    }

  private def urBannerEnabled(config: BannerConfig)(implicit request: Request[_]): Boolean =
    getUrBannerDetailsForPage match {
      case Some(urBanner) => urBanner.isEnabled
      case None           => config.showHelpImproveBanner
    }
}
