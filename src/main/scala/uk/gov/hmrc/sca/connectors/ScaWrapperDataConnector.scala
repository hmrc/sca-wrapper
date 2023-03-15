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
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.{WrapperDataRequest, WrapperDataResponse}

import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperDataConnector @Inject()(http: HttpClient, appConfig: AppConfig) extends Logging {


  def wrapperData(signoutUrl: String)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                      request: Request[AnyContent]): Future[WrapperDataResponse] = {
    val lang = request.cookies.get("PLAY_LANG").map(_.value).getOrElse("en")
    http.POST[WrapperDataRequest, WrapperDataResponse](s"${appConfig.scaWrapperDataUrl}/wrapper-data",
      WrapperDataRequest(appConfig.versionNum, lang, signoutUrl)).recover {
      case ex: Exception =>
        logger.error("FALLBACK!!!!!!!")
        appConfig.fallbackWrapperDataResponse(Lang(lang))
    }
  }

}
