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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import utils.WireMockHelper


class ScaWrapperDataConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with HttpClientSupport with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val rh: RequestHeader = FakeRequest("", "")

  override protected def urlConfigKeys: String = "sca-wrapper.services.single-customer-account-wrapper-data.url"

  val url = s"/single-customer-account-wrapper-data/wrapper-data?lang=en&version=1.0.3"

  private lazy val scaWrapperDataConnector: ScaWrapperDataConnector = injector.instanceOf[ScaWrapperDataConnector]

  override def bindings: Seq[GuiceableModule] =
    Seq(
      inject.bind[HttpClient].toInstance(httpClient)
    )

  "ScaWrapperDataConnector" must {
    "return correct response from Wrapper data" in {
      val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
      val menuItemConfig1: MenuItemConfig = MenuItemConfig("home", "Account home", "http://localhost:9232/personal-account", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None)

      val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(Seq(menuItemConfig1), ptaMenuConfig)
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
        get(urlEqualTo(url))
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

    //    "return the fallback wrapper data response when an exception occurs" in {
    ////      val menuItem: MenuItemConfig = MenuItemConfig("text", "href", true, 1, None, None)
    //
    //      val pertaxUrl: String = "sca-wrapper.services.pertax-frontend.url/personal-account"
    //      val trackingUrl = "sca-wrapper.services.tracking-frontend.url"
    //      def fallbackMenuConfig(implicit lang: Lang): Seq[MenuItemConfig] = Seq(
    //        MenuItemConfig(messages("sca-wrapper.fallback.menu.home"), s"$pertaxUrl", leftAligned = true,
    //          position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    //        MenuItemConfig(messages("sca-wrapper.fallback.menu.messages"), s"$pertaxUrl/messages", leftAligned = false, position = 0, None, None),
    //        MenuItemConfig(messages("sca-wrapper.fallback.menu.progress"), s"$trackingUrl/track", leftAligned = false, position = 1, None, None),
    //        MenuItemConfig(messages("sca-wrapper.fallback.menu.profile"), s"$pertaxUrl/profile-and-settings", leftAligned = false, position = 2, None, None),
    //        MenuItemConfig(messages("sca-wrapper.fallback.menu.signout"), s"$pertaxUrl/signout/feedback/PERTAX", leftAligned = false, position = 3, None, None)
    //      )
    //      val ptaMinMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig("menuName", "backName")
    //      val expectedFallbackResponse = WrapperDataResponse(fallbackMenuConfig(any()), ptaMinMenuConfig)
    //
    //      // Mock the behavior of HttpClient to throw an exception
    //      server.stubFor(
    //        WireMock.get(urlEqualTo(url))
    //          .willReturn(
    //            notFound()
    //          )
    //      )
    //      // Mock the behavior of AppConfig
    ////      when(mockAppConfig.fallbackWrapperDataResponse(any[Lang])).thenReturn(expectedFallbackResponse)
    //
    //      // Call the method being tested
    //      val result = mockConnector.wrapperData()(ec, hc, rh)
    //
    //      // Verify the response
    //      result.map { response =>
    //        assert(response == expectedFallbackResponse)
    //      }
    //    }

  }


}

