/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContentAsEmpty, RequestHeader, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.filters.WrapperDataFilter
import uk.gov.hmrc.sca.utils.Keys

import scala.concurrent.Future

class WrapperDataFilterSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]

  val modules: Seq[GuiceableModule] =
    Seq(inject.bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector))

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  override def beforeEach(): Unit = {
    reset(mockScaWrapperDataConnector)
    when(mockScaWrapperDataConnector.wrapperDataWithMessages()(any(), any(), any()))
      .thenReturn(Future.successful(Some(wrapperDataResponse)))
  }

  val wrapperDataFilter: WrapperDataFilter = application.injector.instanceOf[WrapperDataFilter]

  "WrapperDataFilter" must {

    "attach wrapperData when request is authenticated and not excluded" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/not-excluded").withSession("authToken" -> "valid-token")

      val f: RequestHeader => Future[Result] =
        r => Future.successful(Ok(Json.obj("wrapperData" -> r.attrs.get(Keys.wrapperDataKey))))

      val result = wrapperDataFilter.apply(f)(request)

      status(result) mustBe OK

      verify(mockScaWrapperDataConnector, times(1)).wrapperDataWithMessages()(any(), any(), any())

      contentAsJson(result) mustBe Json.obj(
        "wrapperData" -> Some(wrapperDataResponse)
      )
    }

    Seq("/assets", "/ping/ping").foreach { path =>
      s"not attach wrapperData when request is authenticated but path is excluded: $path" in {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest("GET", path).withSession("authToken" -> "valid-token")

        val f: RequestHeader => Future[Result] =
          r => Future.successful(Ok(Json.obj("wrapperData" -> r.attrs.get(Keys.wrapperDataKey))))

        val result = wrapperDataFilter.apply(f)(request)

        status(result) mustBe OK

        verify(mockScaWrapperDataConnector, never()).wrapperDataWithMessages()(any(), any(), any())

        contentAsJson(result) mustBe Json.obj(
          "wrapperData" -> Json.toJson(None)
        )

      }
    }

    "not attach wrapperData when request is unauthenticated" in {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/not-excluded")

      val f: RequestHeader => Future[Result] =
        r => Future.successful(Ok(Json.obj("wrapperData" -> r.attrs.get(Keys.wrapperDataKey))))

      val result = wrapperDataFilter.apply(f)(request)

      status(result) mustBe OK

      verify(mockScaWrapperDataConnector, never()).wrapperDataWithMessages()(any(), any(), any())

      contentAsJson(result) mustBe Json.obj(
        "wrapperData" -> Json.toJson(None)
      )
    }
  }
}

object WrapperDataFilterSpec {

  import uk.gov.hmrc.sca.models._

  val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
    menuItemConfig = Seq(
      MenuItemConfig(
        "home",
        "Account home",
        "pertaxUrl",
        leftAligned = true,
        position = 0,
        Some("hmrc-account-icon hmrc-account-icon--home"),
        None
      ),
      MenuItemConfig("messages", "Messages", "pertaxUrl/messages", leftAligned = false, position = 0, None, None)
    ),
    ptaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back"),
    urBanners = List(UrBanner("test-page", "test-link", isEnabled = true)),
    webchatPages = List.empty,
    unreadMessageCount = Some(3),
    trustedHelper = Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", Some("principalNino")))
  )
}
