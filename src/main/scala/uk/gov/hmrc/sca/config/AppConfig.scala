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

package uk.gov.hmrc.sca.config

import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.sca.models.{MenuItemConfig, WrapperDataResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val xxx = configuration.get[String]("test1")
  val host: String = configuration.get[String]("host")
  val versionNum: String = "1.0.0"

  val scaWrapperDataUrl = s"${configuration.get[String]("sca-wrapper.internal.single-customer-account-wrapper-data.url")}/single-customer-account-wrapper-data"

  val exitSurveyServiceName: String = configuration.get[String]("sca-wrapper.exit-survey-service-name")
  val feedbackServiceName: String = configuration.get[String]("sca-wrapper.feedback-service-name")

  val signoutBaseUrl = configuration.get[String]("sca-wrapper.signout.url")
  val signoutBaseUrlAlt = configuration.get[Option[String]]("sca-wrapper.signout.alternative-url")

  def exitSurveyUrl(feedbackFrontendUrl: String)(implicit request: RequestHeader): String = s"$feedbackFrontendUrl/$exitSurveyServiceName"

  def feedbackUrl(contactFrontendUrl: String)(implicit request: RequestHeader): String =
    s"$contactFrontendUrl?service=$feedbackServiceName&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val timeout: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.countdown")
  val welshToggle: Boolean = configuration.get[Boolean]("sca-wrapper.welsh-enabled")

  private val fallbackPertaxUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.pertax-frontend.url")}/personal-account"
  private val fallbackBusinessTaxAccountUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.business-tax-frontend.url")}/business-account"
  private val fallbackFeedbackFrontendUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.feedback-frontend.url")}/feedback"
  private val fallbackContactUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.contact-frontend.url")}/contact/beta-feedback"
  private val fallbackAccessibilityStatementUrl: String = configuration.get[String]("sca-wrapper.fallback.accessibility-statement-frontend.url")

  val serviceUrl: String = configuration.get[String]("sca-wrapper.service.url")
  val ggLoginContinueUrl: String = configuration.get[String]("sca-wrapper.service.url")
  val ggSigninUrl: String = configuration.get[String]("sca-wrapper.fallback.gg.signin.url")

  private val fallbackMenuConfig: Seq[MenuItemConfig] = Seq(
    MenuItemConfig("Account Home", s"${fallbackPertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    MenuItemConfig("Messages", s"${fallbackPertaxUrl}/messages", leftAligned = false, position = 0, None, None),
    MenuItemConfig("Check progress", s"${fallbackPertaxUrl}/track", leftAligned = false, position = 1, None, None),
    MenuItemConfig("Profile and settings", s"${fallbackPertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
    MenuItemConfig("Business tax account", s"${fallbackBusinessTaxAccountUrl}", leftAligned = false, position = 3, None, None),
    MenuItemConfig("Sign out", s"$fallbackPertaxUrl/signout/feedback/PERTAX", leftAligned = false, position = 4, None, None)
  )

  val fallbackWrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
    fallbackFeedbackFrontendUrl, fallbackContactUrl, fallbackBusinessTaxAccountUrl,
    fallbackPertaxUrl, fallbackAccessibilityStatementUrl, ggSigninUrl, fallbackMenuConfig
  )
}
