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
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{BannerConfig, MenuItemConfig, PtaMenuConfig, WrapperDataResponse}
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, ScaLayout}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class WrapperService @Inject()(ptaMenuBar: PtaMenuBar,
                               scaLayout: ScaLayout,
                               scaWrapperDataConnector: ScaWrapperDataConnector,
                               appConfig: AppConfig)
                              (implicit ec: ExecutionContext) extends Logging {

  lazy val defaultBannerConfig: BannerConfig = BannerConfig(
    showChildBenefitBanner = appConfig.showChildBenefitBanner,
    showAlphaBanner = appConfig.showAlphaBanner,
    showBetaBanner = appConfig.showBetaBanner,
    showHelpImproveBanner = appConfig.showHelpImproveBanner
  )

  def layout(content: HtmlFormat.Appendable,
             pageTitle: Option[String] = None,
             serviceNameKey: Option[String] = appConfig.serviceNameKey,
             serviceNameUrl: Option[String] = None,
             sidebarContent: Option[Html] = None,
             signoutUrl: String = appConfig.signoutUrl,
             keepAliveUrl: String = appConfig.keepAliveUrl,
             showBackLinkJS: Boolean = false,
             backLinkUrl: Option[String] = None,
             showSignOutInHeader: Boolean = false,
             scripts: Seq[HtmlFormat.Appendable] = Seq.empty,
             styleSheets: Seq[HtmlFormat.Appendable] = Seq.empty,
             bannerConfig: BannerConfig = defaultBannerConfig,
             optTrustedHelper: Option[TrustedHelper] = None,
             fullWidth: Boolean = false,
             hideMenuBar: Boolean = false,
             disableSessionExpired: Boolean = appConfig.disableSessionExpired
            )
            (implicit messages: Messages,
             hc: HeaderCarrier,
             request: Request[AnyContent]): Future[HtmlFormat.Appendable] = {

    logger.info("[SCA Wrapper Library][WrapperService][layout] Wrapper request received")

    scaWrapperDataConnector.wrapperData(signoutUrl).map { wrapperDataResponse =>
      scaLayout(
        menu = ptaMenuBar(sortMenuItemConfig(wrapperDataResponse)),
        serviceNameKey = serviceNameKey,
        serviceNameUrl = serviceNameUrl,
        pageTitle = pageTitle,
        sidebarContent = sidebarContent,
        signoutUrl = signoutUrl,
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
        optTrustedHelper = optTrustedHelper
      )(content)
    }
  }

  def safeSignoutUrl(continueUrl: Option[RedirectUrl] = None): Option[String] = continueUrl match {
    case Some(continue) if continue.getEither(OnlyRelative).isRight => Some(continue.getEither(OnlyRelative).toOption.get.url)
    case _ => appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))
  }

  private def sortMenuItemConfig(wrapperDataResponse: WrapperDataResponse): PtaMenuConfig = {
    val setSignout = setSignoutUrl(wrapperDataResponse.menuItemConfig)
    PtaMenuConfig(
      leftAlignedItems = setSignout.filter(_.leftAligned).sortBy(_.position),
      rightAlignedItems = setSignout.filterNot(_.leftAligned).sortBy(_.position),
      ptaMinMenuConfig = wrapperDataResponse.ptaMinMenuConfig)
  }

  private def setSignoutUrl(menuItemConfig: Seq[MenuItemConfig]) = {
    Try {
      menuItemConfig.find(_.signout).fold(menuItemConfig) { signout =>
        menuItemConfig.updated(menuItemConfig.indexWhere(_.signout), signout.copy(href = appConfig.signoutUrl))
      }
    } match {
      case Success(config) => config
      case Failure(exception) =>
        logger.error(s"[SCA Wrapper Library][WrapperService][setSignoutUrl] Set signout url exception: ${exception.getMessage}")
        menuItemConfig
    }
  }
}
