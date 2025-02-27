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

package uk.gov.hmrc.sca.filters

import org.apache.pekko.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.logging.Logging
import uk.gov.hmrc.sca.models.{Authenticated, Unauthenticated}
import uk.gov.hmrc.sca.utils.Keys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WrapperDataFilter @Inject() (scaWrapperDataConnector: ScaWrapperDataConnector)(implicit
  val executionContext: ExecutionContext,
  val mat: Materializer
) extends Filter
    with Logging {

  private val excludedPaths: Seq[String] = Seq("/assets", "/ping/ping")

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)
    implicit val head: RequestHeader          = rh

    val authenticationStatus =
      (rh.session.get("authToken").isEmpty, excludedPaths.exists(rh.path.contains(_))) match {
        case (_, true) => Unauthenticated
        case (true, _) =>
          logger.info(s"[SCA Wrapper Data Filter][Auth Token Empty]")
          Unauthenticated
        case _         => Authenticated
      }

    val updatedRH = rh.addAttr(Keys.wrapperAuthenticationStatusKey, authenticationStatus)
    if (authenticationStatus == Unauthenticated) {
      f(updatedRH)
    } else {
      for {
        wrapperDataResponse <- scaWrapperDataConnector.wrapperData()
        messageDataResponse <- scaWrapperDataConnector.messageData()
        result              <-
          f(
            updatedRH
              .addAttr(Keys.wrapperDataKey, wrapperDataResponse)
              .addAttr(Keys.messageDataKey, messageDataResponse)
          )
      } yield result
    }
  }
}
