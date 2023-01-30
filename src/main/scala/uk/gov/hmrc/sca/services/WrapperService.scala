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
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.controllers.actions.AuthAction
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMenuConfig, WrapperDataRequest}
import uk.gov.hmrc.sca.views.html.{PtaLayout, PtaMenuBar}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

class WrapperService @Inject()(val controllerComponents: MessagesControllerComponents, authenticate: AuthAction, ptaMenuBar: PtaMenuBar, ptaLayout: PtaLayout, scaWrapperDataConnector: ScaWrapperDataConnector, appConfig: AppConfig) extends FrontendBaseController {

  private def sortMenuItemConfig(menuItemConfig: Seq[MenuItemConfig]): PtaMenuConfig = {
    PtaMenuConfig(menuItemConfig.filter(_.leftAligned).sortBy(_.position), menuItemConfig.filterNot(_.leftAligned).sortBy(_.position))
  }

  def layout(content: HtmlFormat.Appendable,
             pageTitle: Option[String] = None,
             signoutUrl: String = appConfig.ggSignoutUrl,
             keepAliveUrl: String,
             showBackLink: Boolean = false,
             timeout: Boolean = true,
             headerHomeUrl: Option[String] = None,
             backLinkID: Boolean = true,
             backLinkUrl: String = "#",
             showSignOutInHeader: Boolean = false)
            (implicit ec: ExecutionContext,
             hc: HeaderCarrier,
             request: Request[AnyContent],
             messages: Messages): Future[HtmlFormat.Appendable] = {
    scaWrapperDataConnector.wrapperData(WrapperDataRequest(appConfig.signoutUrl)).map { menuItems =>
      ptaLayout(
        menu = ptaMenuBar(sortMenuItemConfig(menuItems.menuItemConfig)),
        pageTitle = pageTitle,
        signoutUrl = signoutUrl,
        keepAliveUrl = keepAliveUrl,
        showBackLink = showBackLink,
        timeout = timeout,
        headerHomeUrl = headerHomeUrl,
        backLinkID = backLinkID,
        backLinkUrl = backLinkUrl,
        showSignOutInHeader = showSignOutInHeader
      )(content)
    }
  }

  def keepAliveAuthenticated: Action[AnyContent] = authenticate { implicit request => Ok }

  def keepAliveUnauthenticated: Action[AnyContent] = Action { implicit request => Ok }

}
