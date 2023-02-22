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
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{OnlyRelative, RedirectUrl}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMenuConfig}
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, ScaLayout}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WrapperService @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                ptaMenuBar: PtaMenuBar,
                                scaLayout: ScaLayout,
                                scaWrapperDataConnector: ScaWrapperDataConnector,
                                appConfig: AppConfig)
                              (implicit ec: ExecutionContext) extends FrontendBaseController {

  private def sortMenuItemConfig(menuItemConfig: Seq[MenuItemConfig]): PtaMenuConfig = {
    val setSignout = setSigoutUrl(menuItemConfig)
    PtaMenuConfig(
      leftAlignedItems = setSignout.filter(_.leftAligned).sortBy(_.position),
      rightAlignedItems = setSignout.filterNot(_.leftAligned).sortBy(_.position))
  }

  private def setSigoutUrl(menuItemConfig: Seq[MenuItemConfig]) = {
    menuItemConfig.find(_.signout).fold(menuItemConfig){ signout =>
      menuItemConfig.updated(menuItemConfig.indexWhere(_.signout), signout.copy(href = appConfig.signoutUrl))
    }
  }

  def layout(content: HtmlFormat.Appendable,
             pageTitle: Option[String] = None,
             serviceNameKey: Option[String] = appConfig.serviceNameKey,
             serviceNameUrl: Option[String] = None,
             signoutUrl: String = appConfig.signoutUrl,
             keepAliveUrl: String = appConfig.keepAliveAuthenticatedUrl,
             showBackLink: Boolean = false,
             timeout: Boolean = true,
             backLinkID: Boolean = true,
             backLinkUrl: String = "#",
             showSignOutInHeader: Boolean = false)
            (implicit messages: Messages,
             hc: HeaderCarrier,
             request: Request[AnyContent]): Future[HtmlFormat.Appendable] = {
    scaWrapperDataConnector.wrapperData(signoutUrl).map { wrapperDataResponse =>
      scaLayout(
        menu = ptaMenuBar(sortMenuItemConfig(wrapperDataResponse.menuItemConfig)),
        serviceNameKey = serviceNameKey,
        pageTitle = pageTitle,
        signoutUrl = signoutUrl,
        keepAliveUrl = keepAliveUrl,
        showBackLink = showBackLink,
        timeout = timeout,
        serviceNameUrl = serviceNameUrl,
        backLinkID = backLinkID,
        backLinkUrl = backLinkUrl,
        showSignOutInHeader = showSignOutInHeader,
        wrapperDataResponse = wrapperDataResponse
      )(content)
    }
  }

  def safeSignoutUrl(continueUrl: Option[RedirectUrl] = None): Option[String] = {
    (continueUrl) match {
      case Some(continue) if continue.getEither(OnlyRelative).isRight => Some(continue.getEither(OnlyRelative).right.get.url)
      case _ => appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))
    }
  }

  final val keepAliveAuthenticatedUrl: String = appConfig.keepAliveAuthenticatedUrl
  final val keepAliveUnauthenticatedUrl: String = appConfig.keepAliveAuthenticatedUrl
}
