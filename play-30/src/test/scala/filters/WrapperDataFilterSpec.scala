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

package filters

import filters.WrapperDataFilterSpec.wrapperDataResponse
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.{Application, inject}
import play.twirl.api.Html
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.filters.WrapperDataFilter
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import uk.gov.hmrc.sca.utils.Keys

import scala.concurrent.Future

class WrapperDataFilterSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  override def beforeEach(): Unit = {
    reset(mockScaWrapperDataConnector)
    when(mockScaWrapperDataConnector.wrapperData()(any(), any(), any())).thenReturn(Future.successful(wrapperDataResponse))
    when(mockScaWrapperDataConnector.messageData()(any(), any())).thenReturn(Future.successful(Some(2)))
  }

  private def wrapperDataFilter = application.injector.instanceOf[WrapperDataFilter]

  "WrapperDataFilter" must {

    "return the request with api responses when request path is not excluded" in {

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/non-excluded-path")

      implicit val materializer: Materializer = mock[Materializer]

      val f: RequestHeader => Future[Result] = r => {
        Future.successful(
          Ok(
            Json.obj(
              "wrapperData" -> r.attrs.get(Keys.wrapperDataKey),
              "messageData" -> r.attrs.get(Keys.messageDataKey)
            )
          )
        )
      }

      val result = wrapperDataFilter.apply(f)(request.withSession("authToken" -> "123abc"))

      status(result) mustBe OK

      verify(mockScaWrapperDataConnector, times(1)).wrapperData()(any(), any(), any())
      verify(mockScaWrapperDataConnector, times(1)).messageData()(any(), any())

      contentAsJson(result) mustBe Json.obj(
        "wrapperData" -> Some(wrapperDataResponse),
        "messageData" -> Some(2)
      )

    }

    "return the request without API responses when request path is not excluded and auth token is not present" in {

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/non-excluded-path")

      implicit val materializer: Materializer = mock[Materializer]

      val f: RequestHeader => Future[Result] = r => {
        Future.successful(
          Ok(
            Json.obj(
              "wrapperData" -> r.attrs.get(Keys.wrapperDataKey),
              "messageData" -> r.attrs.get(Keys.messageDataKey)
            )
          )
        )
      }

      val result = wrapperDataFilter.apply(f)(request)

      status(result) mustBe OK

      verify(mockScaWrapperDataConnector, never()).wrapperData()(any(), any(), any())
      verify(mockScaWrapperDataConnector, never()).messageData()(any(), any())

      contentAsJson(result) mustBe Json.obj(
        "wrapperData" -> None,
        "messageData" -> None
      )

    }

    wrapperDataFilter.excludedPaths.foreach { path =>
      s"return the request without calling the external api when request path contains $path" in {

        implicit val materializer: Materializer = mock[Materializer]
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path)

        val f: RequestHeader => Future[Result] = r => {
          Future.successful(
            Ok(
              Json.obj(
                "wrapperData" -> r.attrs.get(Keys.wrapperDataKey),
                "messageData" -> r.attrs.get(Keys.messageDataKey)
              )
            )
          )
        }

        val result = wrapperDataFilter.apply(f)(request)

        status(result) mustBe OK

        verify(mockScaWrapperDataConnector, never()).wrapperData()(any(), any(), any())
        verify(mockScaWrapperDataConnector, never()).messageData()(any(), any())

        contentAsJson(result) mustBe Json.obj(
          "wrapperData" -> None,
          "messageData" -> None
        )

      }
    }
  }
}

object WrapperDataFilterSpec {

  val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
  val menuItemConfig1: MenuItemConfig = MenuItemConfig("home", "Account home", "pertaxUrl", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None)
  val menuItemConfig2: MenuItemConfig = MenuItemConfig("messages", "Messages", "pertaxUrl-messages", leftAligned = false, position = 0, None, None)
  val menuItemConfig3: MenuItemConfig = MenuItemConfig("progress", "Check progress", "trackingUrl-track", leftAligned = false, position = 1, None, None)
  val menuItemConfig4: MenuItemConfig = MenuItemConfig("profile", "Profile and settings", "pertaxUrl-profile-and-settings", leftAligned = false, position = 2, None, None)
  val menuItemConfig5: MenuItemConfig = MenuItemConfig("signout", "Sign out", "pertaxUrl-signout-feedback-PERTAX", leftAligned = false, position = 3, None, None)
  val menu: Html = Html("\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\"\n   class=\"hmrc-account-menu__link hmrc-account-menu__link--home\n   \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Account menu\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n    <span class=\"hmrc-notification-badge\">2</span>\n\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"Signout-Url\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n")

  private val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5), ptaMenuConfig)

}