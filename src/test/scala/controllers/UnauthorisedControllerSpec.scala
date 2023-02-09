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

package controllers

import fixtures.BaseSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.ws.WSClient
import play.api.test.Helpers
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.controllers.{KeepAliveController, UnauthorisedController}
import uk.gov.hmrc.sca.controllers.actions.AuthAction
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.views.html.UnauthorisedView
import fixtures.RetrievalOps.Ops
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength, Enrolments}

import scala.concurrent.Future

class UnauthorisedControllerSpec extends BaseSpec {

  override implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
  lazy val view = injector.instanceOf[UnauthorisedView]
  lazy val wrapperService = mock[WrapperService]

  private val controller = new UnauthorisedController(Helpers.stubMessagesControllerComponents(), view, wrapperService, appConfig)
  val nino = "AA999999A"

  "UnauthorisedController" must {
    "return a 401" in {
      when(wrapperService.layout(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any())(any(), any(), any()))
        .thenReturn(Future.successful(HtmlFormat.raw("test")))

      val result = controller.onPageLoad()(fakeRequest)
      whenReady(result) { res =>
        res.header.status shouldBe 401
      }
    }
  }
}
