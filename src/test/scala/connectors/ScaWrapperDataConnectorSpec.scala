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

package connectors


import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import utils.WireMockHelper

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class ScaWrapperDataConnectorSpec extends AsyncWordSpec with WireMockHelper with ScalaFutures with Matchers with HttpClientSupport with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val rh: RequestHeader = FakeRequest("", "")

  override protected def urlConfigKeys: String = "sca-wrapper.services.single-customer-account-wrapper-data.url"

  val urlWrapperDataResponse = s"/single-customer-account-wrapper-data/wrapper-data?lang=en&version=1.0.3"
  val urlMessageData = "/single-customer-account-wrapper-data/message-data"

  private lazy val scaWrapperDataConnector: ScaWrapperDataConnector = injector.instanceOf[ScaWrapperDataConnector]

  override def bindings: Seq[GuiceableModule] =
    Seq(
      inject.bind[HttpClient].toInstance(httpClient)
    )

  "ScaWrapperDataConnector" must {

    "trigger a timeout if the request for wrapper data takes more than 1 second" in {

      server.stubFor(
        get(anyUrl()).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("You haven't seen me, right!")
            .withFixedDelay(2000)
        )
      )

      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds))

      val config = app.injector.instanceOf(classOf[AppConfig])

      val wrapperDataResponse = scaWrapperDataConnector.wrapperData()(scala.concurrent.ExecutionContext.global, hc, rh)

      wrapperDataResponse.isReadyWithin(1 second) mustBe false
      whenReady(wrapperDataResponse) { result =>
        server.verify(getRequestedFor(urlPathMatching("/single-customer-account-wrapper-data/wrapper-data.*")))
        result mustBe wrapperDataResponse.futureValue
      }
    }

    "return a successful response when wrapperData() is called" in {
      val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
      val menuItemConfig: MenuItemConfig = MenuItemConfig("home", "Account home", "http://localhost:9232/personal-account",
        leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None)

      val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(Seq(menuItemConfig), ptaMenuConfig)
      val wrapperDataJsonResponse =
        """
          |{
          |    "menuItemConfig": [
          |        {
          |            "id": "home",
          |            "text": "Account home",
          |            "href": "http://localhost:9232/personal-account",
          |            "leftAligned": true,
          |            "position": 0,
          |            "icon": "hmrc-account-icon hmrc-account-icon--home"
          |        }
          |    ],
          |    "ptaMinMenuConfig": {
          |        "menuName": "Account menu",
          |        "backName": "Back"
          |    }
          |}
          |""".stripMargin

      server.stubFor(
        get(urlEqualTo(urlWrapperDataResponse))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(wrapperDataJsonResponse)
          )
      )

      scaWrapperDataConnector.wrapperData().map { response =>
        response mustBe wrapperDataResponse
      }
    }

    "return the fallback wrapper data response when wrapperData() is called but wrapper data is not available" in {

      def fallbackMenuConfig: Seq[MenuItemConfig] = Seq(
        MenuItemConfig("home", "Account home", s"http://localhost:9232/personal-account", leftAligned = true,
          position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig("messages", "Messages", s"http://localhost:9232/personal-account/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig("progress", "Check progress", s"http://localhost:9100/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig("profile", "Profile and settings", s"http://localhost:9232/personal-account/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig("signout", "Sign out", s"http://localhost:9232/personal-account/signout/feedback/PERTAX", leftAligned = false, position = 3, None, None)
      )

      def fallbackWrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
        fallbackMenuConfig, PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
      )

      server.stubFor(
        get(urlEqualTo(urlWrapperDataResponse))
          .willReturn(
            badRequest()
              .withHeader("Content-Type", "application/json")
          )
      )

      scaWrapperDataConnector.wrapperData().map { response =>
        response mustBe fallbackWrapperDataResponse
      }
    }

    "return a successful response when messageData() is called" in {

      server.stubFor(
        get(urlEqualTo(urlMessageData))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(1.toString)
          )
      )

      scaWrapperDataConnector.messageData().map { response =>
        response mustBe Some(1)
      }
    }

    "return None when messageData() is called but an exception occurs" in {

      server.stubFor(
        get(urlEqualTo(urlMessageData))
          .willReturn(
            badRequest()
              .withHeader("Content-Type", "application/json")
              .withBody("invalid body")
          )
      )

      scaWrapperDataConnector.messageData().map { response =>
        response mustBe None
      }
    }
  }
}
