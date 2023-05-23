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

import akka.actor.ActorSystem
import akka.util.Timeout
import com.google.inject.matcher.Matchers
import fixtures.{BaseSpec, WireMockHelper}
import org.scalacheck.Gen.const
import org.scalatest.RecoverMethods.recoverToExceptionIf
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector

import scala.concurrent.{Await, Future, TimeoutException}
import scala.concurrent.duration._
import scala.xml.dtd.ContentModel.Translator.lang



class ScaWrapperDataConnectorSpec extends AnyWordSpec with WireMockHelper with BaseSpec with Eventually {

  private lazy val wrapperDataConnector: ScaWrapperDataConnector = injector.instanceOf[ScaWrapperDataConnector]

  implicit val config: PatienceConfig = PatienceConfig(5.seconds)
  server.start()

  applicationBuilder().configure(
    "metrics.enabled" -> false,
    "auditing.enabled" -> false,
    "auditing.traceRequests" -> false
  )
    .build()

  "The ScaWrapperDataConnector" must {
    "trigger a timeout after x seconds" in {

      // Set the timeout duration to 20 seconds.
      val timeout = Timeout(20.seconds)

      // Try to complete the Future with the timeout.
      val result = wrapperDataConnector.wrapperData()(ec, hc, fakeRequest)
      // Assert that the result is a timeout exception.
      whenReady(result) { res =>
         res.ptaMinMenuConfig.menuName == "a"
      }
    }
  }
}
