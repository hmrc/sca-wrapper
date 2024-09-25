/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.sca.config

import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.hmrcfrontend.config.ContactFrontendConfig

import java.net.URLEncoder
import javax.inject.Inject

class LocalContactFrontendConfig @Inject() (config: Configuration) extends ContactFrontendConfig(config) {
  override def referrerUrl(implicit request: RequestHeader): Option[String] =
    Some(s"${config.get[String]("platform.frontend.host")}${request.uri}")

  def url(implicit request: RequestHeader): Option[String] =
    for {
      contactFrontendBaseUrl <- baseUrl
      path                   <- config.getOptional[String]("contact-frontend.path")
      serviceId              <- serviceId
      referrer               <- referrerUrl
    } yield s"$contactFrontendBaseUrl$path?service=${URLEncoder.encode(serviceId, "UTF-8")}&backUrl=${URLEncoder.encode(referrer, "UTF-8")}"
}
