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

package utils.scala2

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.inject.bind
import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc.{AnyContentAsEmpty, Cookie, Cookies}
import play.api.test.FakeRequest
import play.twirl.api.Html
import service.WrapperServiceSpec._
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.{Webchat, WrapperDataResponse}
import uk.gov.hmrc.sca.utils.{Keys, WebchatUtil}
import uk.gov.hmrc.webchat.client.WebChatClient
import utils.BaseSpec

class WebchatUtilSpec extends BaseSpec {
  private val mockAppConfig                    = mock[AppConfig]
  private val mockWebChatClient: WebChatClient = mock[WebChatClient]

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .overrides(
      bind[AppConfig].toInstance(mockAppConfig),
      bind[WebChatClient].toInstance(mockWebChatClient)
    )
    .build()

  private val sut = new WebchatUtil(mockAppConfig, app.injector)

  override def beforeEach(): Unit = {
    reset(mockWebChatClient)
    super.beforeEach()
    when(mockWebChatClient.loadRequiredElements()(any())).thenReturn(Some(Html("some1")))
    when(mockWebChatClient.loadHMRCChatSkinElement(any())(any())).thenReturn(Some(Html("some2")))
    when(mockAppConfig.webChatHashingKey).thenReturn(Some("value1"))
    when(mockAppConfig.webChatKey).thenReturn(Some("value2"))

  }

  "WebchatUtil.getWebchatScripts" should {

    "match exactly" in {
      val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
        Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
        ptaMenuConfig,
        List(defaultUrBanner),
        List(Webchat("/test-page", "popup", isEnabled = true))
      )

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/test-page")
        .withAttrs(
          TypedMap(
            Keys.wrapperDataKey    -> wrapperDataResponse,
            Keys.messageDataKey    -> 2,
            RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
          )
        )

      val result = sut.getWebchatScripts(request)
      result mustBe Seq(Html("some1"), Html("some2"))
    }

    "return no items when config missing and enabled" in {
      val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
        Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
        ptaMenuConfig,
        List(defaultUrBanner),
        List(Webchat("/test-page", "popup", isEnabled = true))
      )

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/test-page")
        .withAttrs(
          TypedMap(
            Keys.wrapperDataKey    -> wrapperDataResponse,
            Keys.messageDataKey    -> 2,
            RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
          )
        )
      when(mockAppConfig.webChatHashingKey).thenReturn(None)
      when(mockAppConfig.webChatKey).thenReturn(None)
      val result                                       = sut.getWebchatScripts(request)
      result mustBe Nil
    }
    "return empty list when config missing and disabled" in {
      val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
        Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
        ptaMenuConfig,
        List(defaultUrBanner),
        List(Webchat("/test-page", "popup", isEnabled = false))
      )

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/test-page")
        .withAttrs(
          TypedMap(
            Keys.wrapperDataKey    -> wrapperDataResponse,
            Keys.messageDataKey    -> 2,
            RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
          )
        )
      when(mockAppConfig.webChatHashingKey).thenReturn(None)
      when(mockAppConfig.webChatKey).thenReturn(None)
      val result                                       = sut.getWebchatScripts(request)
      result mustBe Nil
    }
  }

  "does not match a prefix" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat("/test-page", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/test-page/sub-page")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq.empty
  }

  "match a prefix" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat("/test-page/.*", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/test-page/sub-page")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq(Html("some1"), Html("some2"))
  }

  "does not match within the request" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat("test-page", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/start/test-page/sub-page")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq.empty
  }

  "match within the request" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat(".*test-page.*", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/start/test-page/sub-page")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq(Html("some1"), Html("some2"))
  }

  "match only the page and not the sub pages" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat(".*test-page", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/start/test-page/sub-page")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq.empty
  }

  "the query string is ignored" in {
    val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
      Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
      ptaMenuConfig,
      List(defaultUrBanner),
      List(Webchat(".*test-page", "popup", isEnabled = true))
    )

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/start/test-page?qs=1")
      .withAttrs(
        TypedMap(
          Keys.wrapperDataKey    -> wrapperDataResponse,
          Keys.messageDataKey    -> 2,
          RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
        )
      )

    val result = sut.getWebchatScripts(request)
    result mustBe Seq(Html("some1"), Html("some2"))
  }
}
