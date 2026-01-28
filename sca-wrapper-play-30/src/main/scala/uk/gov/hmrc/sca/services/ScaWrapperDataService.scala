/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.sca.services

import play.api.Logging
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.WrapperDataResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

class ScaWrapperDataService @Inject() (scaWrapperDataConnector: ScaWrapperDataConnector)(implicit ec: ExecutionContext)
    extends Logging {

  def wrapperDataWithMessages()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    request: RequestHeader
  ): Future[Option[WrapperDataResponse]] = scaWrapperDataConnector.wrapperDataWithMessages()

  def retrieveServiceNavigationToggle()(implicit headerCarrier: HeaderCarrier): Future[Boolean] =
    scaWrapperDataConnector
      .serviceNavigationToggle()
      .map(json =>
        Try((json \ "useNewServiceNavigation").as[Boolean]) match {
          case Success(toggle) => toggle
          case Failure(error)  =>
            logger.error(error.getMessage, error)
            false
        }
      )
      .recover {
        case ex @ UpstreamErrorResponse(_, statusCode, _, _) if statusCode < 499 =>
          logger.error(ex.message, ex)
          false
        case ex @ UpstreamErrorResponse(_, _, _, _)                              =>
          logger.error(ex.message)
          false
        case NonFatal(_)                                                         => false
      }

}
