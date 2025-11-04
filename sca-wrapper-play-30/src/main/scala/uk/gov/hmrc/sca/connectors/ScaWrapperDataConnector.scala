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

package uk.gov.hmrc.sca.connectors

import com.google.inject.Inject
import play.api.Logging
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.WrapperDataResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperDataConnector @Inject() (
  http: HttpClientV2,
  appConfig: AppConfig
) extends Logging {

  def wrapperDataWithMessages()(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    request: RequestHeader
  ): Future[Option[WrapperDataResponse]] = {
    val lang = request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")

    val url = url"${appConfig.scaWrapperDataUrl}/wrapper-data-with-messages?lang=$lang&version=${appConfig.versionNum}"

    logger.debug(
      s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Requesting combined wrapper data with messages - lang: $lang"
    )

    http
      .get(url)
      .transform(_.withRequestTimeout(appConfig.timeoutHttpClientMillis.millis))
      .execute[WrapperDataResponse]
      .map(Some(_))
      .recover {
        case ex: GatewayTimeoutException                       =>
          logger.error(
            s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Time out while calling combined wrapper data: ${ex.getMessage}"
          )
          None
        case ex: UpstreamErrorResponse if ex.statusCode >= 499 =>
          logger.error(
            s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Server error while calling combined wrapper data: ${ex.getMessage}"
          )
          None
        case scala.util.control.NonFatal(ex)                   =>
          logger.error(
            s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperDataWithMessages] Exception while calling combined wrapper data: ${ex.getMessage}",
            ex
          )
          None
      }
  }
}
