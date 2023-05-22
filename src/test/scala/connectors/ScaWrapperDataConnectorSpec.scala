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

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.google.inject.matcher.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}




class ScaWrapperDataConnectorTimeoutSpec extends AnyWordSpec with Matchers {

  "The ScaWrapperDataConnector" must {
    "timeout after x seconds when calling the wrapper data service" in {
      val connector = new ScaWrapperDataConnector(Http(), AppConfig())
      val request = HttpRequest(uri = s"${connector.appConfig.scaWrapperDataUrl}/wrapper-data?lang=en&version=1")
      val responseFuture: Future[HttpResponse] = connector.wrapperData(request, RequestTimeout(1.second))

      responseFuture.onComplete {
        case Success(response) =>
          fail("Expected a timeout")
        case Failure(_: TimeoutException) =>
          succeed
      }
    }
  }
}
