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
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.WrapperDataResponse

import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperDataConnector @Inject()(http: HttpClient, appConfig: AppConfig) extends Logging {


  def wrapperData()(implicit ec: ExecutionContext, hc: HeaderCarrier,
                    request: RequestHeader): Future[WrapperDataResponse] = {
    val lang = request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")
    logger.info(s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperData] Requesting menu config from Wrapper Data- lang: $lang")
    http.GET[WrapperDataResponse](s"${appConfig.scaWrapperDataUrl}/wrapper-data?lang=$lang&version=${appConfig.versionNum}").recover {
      case ex: Exception =>
        logger.error(s"[SCA Wrapper Library][ScaWrapperDataConnector][wrapperData] Exception while calling Wrapper Data: ${ex.getMessage}")
        appConfig.fallbackWrapperDataResponse(Lang(lang))
    }
  }

  def messageData()(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Int]] = {
    logger.info(s"[SCA Wrapper Library][ScaWrapperDataConnector][messageData] Requesting unread message count from Wrapper Data")
    http.GET[Option[Int]](s"${appConfig.scaWrapperDataUrl}/message-data").recover {
      case ex: Exception =>
        logger.error(s"[SCA Wrapper Library][ScaWrapperDataConnector][messageData] Exception while calling Wrapper Data: ${ex.getMessage}")
        None
    }
  }

}
