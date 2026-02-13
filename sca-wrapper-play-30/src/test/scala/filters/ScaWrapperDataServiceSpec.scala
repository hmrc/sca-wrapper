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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.{Application, inject}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.services.ScaWrapperDataService

import scala.concurrent.Future

class ScaWrapperDataServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(
      inject.bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector)
    )
    .build()

  override def beforeEach(): Unit =
    reset(mockScaWrapperDataConnector)

  val sut: ScaWrapperDataService = application.injector.instanceOf[ScaWrapperDataService]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "retrieveServiceNavigationToggle" must {

    "return true" in {
      when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
        Future.successful(Json.obj("useNewServiceNavigation" -> true))
      )

      val result = sut.retrieveServiceNavigationToggle().futureValue

      result mustBe true
    }

    "return false" when {
      "the response is false" in {
        when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
          Future.successful(Json.obj("useNewServiceNavigation" -> false))
        )

        val result = sut.retrieveServiceNavigationToggle().futureValue

        result mustBe false
      }

      "The json is invalid" in {
        when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
          Future.successful(Json.obj())
        )

        val result = sut.retrieveServiceNavigationToggle().futureValue

        result mustBe false
      }

      "The response is a client error" in {
        when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse("error", 400))
        )

        val result = sut.retrieveServiceNavigationToggle().futureValue

        result mustBe false
      }

      "The response is a server error" in {
        when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
          Future.failed(UpstreamErrorResponse("error", 500))
        )

        val result = sut.retrieveServiceNavigationToggle().futureValue

        result mustBe false
      }

      "The response is bad gateway exception" in {
        when(mockScaWrapperDataConnector.serviceNavigationToggle()(using any(), any())).thenReturn(
          Future.failed(new BadGatewayException("error"))
        )

        val result = sut.retrieveServiceNavigationToggle().futureValue

        result mustBe false
      }
    }
  }
}
