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
import uk.gov.hmrc.sca.models.WrapperDataResponse
import uk.gov.hmrc.sca.utils.Keys

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.scalaUtilChainingOps

class WrapperDataFilter @Inject() (scaWrapperDataConnector: ScaWrapperDataConnector)(implicit
  val executionContext: ExecutionContext,
  val mat: Materializer
) extends Filter
    with Logging {

  private val excludedPaths: Seq[String] = Seq("/assets", "/ping/ping")

  private def checkIsAuthenticated(requestHeader: RequestHeader): Boolean =
    (requestHeader.session.get("authToken").isEmpty, excludedPaths.exists(requestHeader.path.contains(_))) match {
      case (_, true) => false
      case (true, _) =>
        logger.info(s"[SCA Wrapper Data Filter][Auth Token Empty]")
        false
      case _         => true
    }

  private def retrieveWrapperData(
    isAuthenticated: Boolean
  )(implicit rh: RequestHeader, headerCarrier: HeaderCarrier): Future[(Option[WrapperDataResponse], Option[Int])] =
    if (isAuthenticated) {
      for {
        optWrapperDataResponse <- scaWrapperDataConnector.wrapperData()
        optMessageDataResponse <- scaWrapperDataConnector.messageData()
      } yield (optWrapperDataResponse, optMessageDataResponse)
    } else {
      Future.successful(Tuple2(None, None))
    }

  private def updateRequestHeader(
    requestHeader: RequestHeader,
    isAuthenticated: Boolean,
    optWrapperDataResponse: Option[WrapperDataResponse],
    optMessageDataResponse: Option[Int]
  ): RequestHeader =
    requestHeader
      .addAttr(Keys.wrapperIsAuthenticatedKey, isAuthenticated)
      .pipe[RequestHeader](rh => optWrapperDataResponse.fold(rh)(wdr => rh.addAttr(Keys.wrapperDataKey, wdr)))
      .pipe[RequestHeader](rh => optMessageDataResponse.fold(rh)(mdr => rh.addAttr(Keys.messageDataKey, mdr)))

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier         = HeaderCarrierConverter.fromRequestAndSession(rh, rh.session)
    implicit val implicitRequestHeader: RequestHeader = rh

    val isAuthenticated = checkIsAuthenticated(rh)
    for {
      (optWrapperData, optMessageData) <- retrieveWrapperData(isAuthenticated)
      updatedRequestHeader              = updateRequestHeader(rh, isAuthenticated, optWrapperData, optMessageData)
      result                           <- f(updatedRequestHeader)
    } yield result
  }
}
