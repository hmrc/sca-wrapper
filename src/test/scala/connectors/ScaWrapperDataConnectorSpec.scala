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
import fixtures.WireMockHelper
import org.scalactic.source.Position
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps





class ScaWrapperDataConnectorSpec extends AnyWordSpec with WireMockHelper with ScalaFutures with must.Matchers {

  "The ScaWrapperDataConnector with default settings" must {
    "trigger a timeout if the request for wrapper data takes more than 1 second" in {

      server.stubFor(
        get(anyUrl()).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("You haven't seen me, right!")
            .withFixedDelay(2000)
        )
      )

      val app = GuiceApplicationBuilder().configure(
        "sca-wrapper.services.single-customer-account-wrapper-data.url" -> s"http://localhost:${server.port}"
      ).build()

      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds))
      val position: Position = Position("SCAWrapperDataConnector.scala","uk/gov/hmrc/sca/connectors/ScaWrapperDataConnector.scala",37)

      running(app) {
        val SUT: ScaWrapperDataConnector = app.injector.instanceOf(classOf[ScaWrapperDataConnector])
        val config = app.injector.instanceOf(classOf[AppConfig])

        val result = SUT.wrapperData()(scala.concurrent.ExecutionContext.global, HeaderCarrier(), FakeRequest())

        result.isReadyWithin(1 second) mustBe false
        result.futureValue(patienceConfig, position) mustBe config.fallbackWrapperDataResponse(Lang.apply("en"))

        server.verify(getRequestedFor(urlPathMatching("/single-customer-account-wrapper-data/wrapper-data.*")))
      }
    }
  }
}
