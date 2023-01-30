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

package uk.gov.hmrc.sca.controllers

import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.viewmodels.ViewUtils.titleNoForm
import uk.gov.hmrc.sca.views.html.UnauthorisedView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnauthorisedController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UnauthorisedView,
                                        wrapperService: WrapperService,
                                        appConfig: AppConfig,
                                        keepAliveController: KeepAliveController
                                      )(implicit messages: Messages, ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    wrapperService.layout(
      content = view(),
      pageTitle = Some(titleNoForm(messages("unauthorised.title"))),
      signoutUrl = appConfig.signoutBaseUrl,
      keepAliveUrl = Redirect(routes.KeepAliveController.keepAliveUnauthenticated).toString()).map { layout =>
      Ok(layout)
    }
  }
}
