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
import play.api.Application
import play.api.inject.bind
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.HttpClientV2Support
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, UrBanner, Webchat, WrapperDataResponse}
import utils.BaseSpec

class ScaWrapperDataConnectorSpec extends BaseSpec with HttpClientV2Support {

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .overrides(bind[HttpClientV2].toInstance(httpClientV2))
    .build()

  private val urlWrapperDataWithMessages =
    "/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3"

  private lazy val scaWrapperDataConnector: ScaWrapperDataConnector = app.injector.instanceOf[ScaWrapperDataConnector]

  val defaultUrBanner: UrBanner = UrBanner("test-page", "test-link", isEnabled = true)
  val defaultWebchat: Webchat   = Webchat("test-page", "popup", isEnabled = true, chatType = "loadHMRCChatSkinElement")

  "ScaWrapperDataConnector" must {

    "return a successful WrapperDataResponse when wrapperDataWithMessages() returns 200 with valid JSON" in {
      val ptaMenuConfig  = PtaMinMenuConfig("Account menu", "Back")
      val menuItemConfig = MenuItemConfig(
        "home",
        "Account home",
        "http://localhost:9232/personal-account",
        leftAligned = true,
        position = 0,
        Some("hmrc-account-icon hmrc-account-icon--home"),
        None
      )

      val expectedResponse = WrapperDataResponse(
        Seq(menuItemConfig),
        ptaMenuConfig,
        List(defaultUrBanner),
        List(defaultWebchat),
        Some(2),
        None
      )

      val jsonResponse =
        s"""
           |{
           |  "menuItemConfig": [
           |    {
           |      "id": "home",
           |      "text": "Account home",
           |      "href": "http://localhost:9232/personal-account",
           |      "leftAligned": true,
           |      "position": 0,
           |      "icon": "hmrc-account-icon hmrc-account-icon--home"
           |    }
           |  ],
           |  "ptaMinMenuConfig": {
           |    "menuName": "Account menu",
           |    "backName": "Back"
           |  },
           |  "urBanners": [
           |    {
           |      "page": "test-page",
           |      "link": "test-link",
           |      "isEnabled": true
           |    }
           |  ],
           |  "webchatPages": [
           |    {
           |      "page": "test-page",
           |      "skin": "popup",
           |      "isEnabled": true,
           |      "chatType": "loadHMRCChatSkinElement"
           |    }
           |  ],
           |  "unreadMessageCount": 2
           |}
           |""".stripMargin

      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(okJson(jsonResponse))
      )

      scaWrapperDataConnector.wrapperDataWithMessages().map { result =>
        result mustBe Some(expectedResponse)
      }
    }

    "return None when wrapperDataWithMessages() returns a server error" in {
      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(serverError())
      )

      scaWrapperDataConnector.wrapperDataWithMessages().map { result =>
        result mustBe None
      }
    }

    "return None when wrapperDataWithMessages() returns invalid JSON" in {
      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(ok("invalid-json"))
      )

      scaWrapperDataConnector.wrapperDataWithMessages().map { result =>
        result mustBe None
      }
    }
  }
}
