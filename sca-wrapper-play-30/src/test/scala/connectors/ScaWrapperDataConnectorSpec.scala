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
import org.mockito.Mockito.when
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.{Application, Logger}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, UrBanner, Webchat, WrapperDataResponse}
import utils.BaseSpec

class ScaWrapperDataConnectorSpec extends BaseSpec with LogCapturing {

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure(
      "play.ws.timeout.request"    -> "1s",
      "play.ws.timeout.connection" -> "1s"
    )
    .build()

  private val urlWrapperDataWithMessages =
    "/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3"

  lazy val testLogger: Logger       = Logger("test-logger")
  lazy val httpClient: HttpClientV2 = app.injector.instanceOf[HttpClientV2]
  lazy val mockAppConfig: AppConfig = mock[AppConfig]

  private lazy val scaWrapperDataConnector: ScaWrapperDataConnector =
    new ScaWrapperDataConnector(httpClient, mockAppConfig) {
      override protected val logger: Logger = testLogger
    }

  val defaultUrBanner: UrBanner = UrBanner("test-page", "test-link", isEnabled = true)
  val defaultWebchat: Webchat   = Webchat("test-page", "popup", isEnabled = true, chatType = "loadHMRCChatSkinElement")

  val jsonResponse: String =
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

  val jsonResponseWithBespokeUrBanner: String =
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
       |      "isEnabled": true,
       |      "titleEn": "Help improve this service",
       |      "titleCy": "Helpu gwella'r gwasanaeth hwn",
       |      "linkTextEn": "Take part",
       |      "linkTextCy": "Cymerwch ran",
       |      "hideCloseButton": false
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
        menuItemConfig = Seq(menuItemConfig),
        ptaMinMenuConfig = ptaMenuConfig,
        urBanners = List(defaultUrBanner),
        webchatPages = List(defaultWebchat),
        unreadMessageCount = Some(2),
        trustedHelper = None
      )

      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")

      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(okJson(jsonResponse))
      )

      scaWrapperDataConnector.wrapperDataWithMessages().map { result =>
        result mustBe Some(expectedResponse)
      }
    }

    "return a successful WrapperDataResponse including bespoke UR banner fields when present in JSON" in {
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

      val bespokeUrBanner = defaultUrBanner.copy(
        titleEn = Some("Help improve this service"),
        titleCy = Some("Helpu gwella'r gwasanaeth hwn"),
        linkTextEn = Some("Take part"),
        linkTextCy = Some("Cymerwch ran"),
        hideCloseButton = Some(false)
      )

      val expectedResponse = WrapperDataResponse(
        menuItemConfig = Seq(menuItemConfig),
        ptaMinMenuConfig = ptaMenuConfig,
        urBanners = List(bespokeUrBanner),
        webchatPages = List(defaultWebchat),
        unreadMessageCount = Some(2),
        trustedHelper = None
      )

      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")

      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(okJson(jsonResponseWithBespokeUrBanner))
      )

      scaWrapperDataConnector.wrapperDataWithMessages().map { result =>
        result mustBe Some(expectedResponse)
      }
    }

    "return None when wrapperDataWithMessages() returns a server error" in {
      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")
      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(serverError())
      )
      withCaptureOfLoggingFrom(testLogger) { logs =>
        val result = scaWrapperDataConnector.wrapperDataWithMessages().futureValue(Timeout(Span(2, Seconds)))
        result mustBe None
        val log    =
          logs.find(log =>
            log.getLevel == ch.qos.logback.classic.Level.ERROR && log.getMessage.contains(
              "Server error while calling combined wrapper data"
            )
          )
        log.map(_.getMessage) mustBe Some(
          s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Server error while calling combined wrapper data: GET of 'http://localhost:${server.port()}/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3' returned 500. Response body: ''"
        )
        log.map(_.getThrowableProxy) mustBe Some(null)
      }
    }

    "return None when wrapperDataWithMessages() returns a client error and log exception" in {
      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")
      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(badRequest())
      )
      withCaptureOfLoggingFrom(testLogger) { logs =>
        val result = scaWrapperDataConnector.wrapperDataWithMessages().futureValue(Timeout(Span(2, Seconds)))
        result mustBe None
        val log    =
          logs.find(log =>
            log.getLevel == ch.qos.logback.classic.Level.ERROR && log.getMessage.contains(
              "Exception while calling combined wrapper data"
            )
          )
        log.map(_.getMessage) mustBe Some(
          s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Exception while calling combined wrapper data: GET of 'http://localhost:${server.port()}/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3' returned 400. Response body: ''"
        )
        log.map(_.getThrowableProxy.getMessage) mustBe Some(
          s"GET of 'http://localhost:${server.port()}/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3' returned 400. Response body: ''"
        )
      }
    }

    "return None when wrapperDataWithMessages() is timing out" in {
      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")
      server.stubFor(
        get(urlEqualTo(urlWrapperDataWithMessages))
          .willReturn(okJson(jsonResponse).withFixedDelay(2000))
      )
      withCaptureOfLoggingFrom(testLogger) { logs =>
        val result = scaWrapperDataConnector.wrapperDataWithMessages().futureValue(Timeout(Span(2, Seconds)))
        result mustBe None
        val log    =
          logs.find(log =>
            log.getLevel == ch.qos.logback.classic.Level.ERROR && log.getMessage.contains(
              "Time out while calling combined wrapper data"
            )
          )
        log.map(_.getMessage) mustBe Some(
          s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Time out while calling combined wrapper data: GET of 'http://localhost:${server.port()}/single-customer-account-wrapper-data/wrapper-data-with-messages?lang=en&version=1.0.3' timed out with message 'Request timeout to localhost/127.0.0.1:${server
              .port()} after 1000 ms'"
        )
        log.map(_.getThrowableProxy) mustBe Some(null)
      }
    }

    "return None when wrapperDataWithMessages() returns invalid JSON" in {
      when(mockAppConfig.scaWrapperDataUrl).thenReturn(
        s"http://localhost:${server.port()}/single-customer-account-wrapper-data"
      )
      when(mockAppConfig.versionNum).thenReturn("1.0.3")

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
