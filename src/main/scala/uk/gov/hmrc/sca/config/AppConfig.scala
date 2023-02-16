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
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.binders.Origin
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.sca.models.{MenuItemConfig, WrapperDataResponse}

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(configuration: Configuration, messages: MessagesApi) {

  //library manual update, MAJOR.MINOR.PATCH
  final val versionNum: String = "1.0.1" //TODO read from build.sbt

  //config for service name in black bar
  val serviceNameKey: Option[String] = configuration.get[Option[String]]("sca-wrapper.service-name.messages-key")

  //service name config for links
  val feedbackServiceName: String = configuration.get[String]("sca-wrapper.feedback-service-name")
  private val host: String = configuration.get[String]("host")
  def feedbackUrl(contactFrontendUrl: String)(implicit request: RequestHeader): String =
    s"$contactFrontendUrl?service=$feedbackServiceName&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"
  private val accessibilityStatementReferrerUrl = configuration.get[String]("sca-wrapper.accessibility-statement.referrer.url")
  private val accessibilityStatementRedirectUrl = configuration.get[String]("sca-wrapper.accessibility-statement.redirect.url")
  def accessibilityStatementUrl(accessibilityBaseUrl: String) =
    s"$accessibilityBaseUrl/accessibility-statement/$accessibilityStatementRedirectUrl?referrerUrl=${SafeRedirectUrl(accessibilityBaseUrl + accessibilityStatementReferrerUrl).encodedUrl}"
  val enc = URLEncoder.encode(_: String, "UTF-8")
  val exitSurveyOrigin: Option[String] = configuration.get[Option[String]]("sca-wrapper.exit-survey-origin")

  //service config
  val timeout: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.countdown")
  val welshToggle: Boolean = configuration.get[Boolean]("sca-wrapper.welsh-enabled")

  //signout links
  private val signoutBaseUrl: String = configuration.get[String]("sca-wrapper.signout.url")
  private val signoutBaseUrlAlt: Option[String] = configuration.get[Option[String]]("sca-wrapper.signout.alternative-url")
  final val signoutUrl: String = signoutBaseUrlAlt.getOrElse(signoutBaseUrl)

  //internal
  final val serviceUrl: String = configuration.get[String]("sca-wrapper.service.url")
  final val keepAliveAuthenticatedUrl: String = s"${serviceUrl}/keep-alive-authenticated"
  final val keepAliveUnauthenticatedUrl: String = s"${serviceUrl}/keep-alive-unauthenticated"

  //external url
  final val scaWrapperDataUrl = s"${configuration.get[String]("sca-wrapper.internal.single-customer-account-wrapper-data.url")}/single-customer-account-wrapper-data"
  final val ggLoginContinueUrl: String = configuration.get[String]("sca-wrapper.service.url")
  final val ggSigninUrl: String = configuration.get[String]("sca-wrapper.fallback.gg.signin.url")

  //fallback urls in the event that wrapper data is offline
  private val fallbackPertaxUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.pertax-frontend.url")}/personal-account"
  private val fallbackBusinessTaxAccountUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.business-tax-frontend.url")}/business-account"
  private val fallbackFeedbackFrontendUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.feedback-frontend.url")}/feedback"
  private val fallbackContactUrl: String = s"${configuration.get[String]("sca-wrapper.fallback.contact-frontend.url")}/contact/beta-feedback"
  private val fallbackAccessibilityStatementUrl: String = configuration.get[String]("sca-wrapper.fallback.accessibility-statement-frontend.url")

  //fallback menu config in the event that wrapper data is offline
  private def fallbackMenuConfig(implicit lang: Lang): Seq[MenuItemConfig] = Seq(
    MenuItemConfig(messages("sca-wrapper.fallback.menu.home"), s"${fallbackPertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    MenuItemConfig(messages("sca-wrapper.fallback.menu.messages"), s"${fallbackPertaxUrl}/messages", leftAligned = false, position = 0, None, None),
    MenuItemConfig(messages("sca-wrapper.fallback.menu.progress"), s"${fallbackPertaxUrl}/track", leftAligned = false, position = 1, None, None),
    MenuItemConfig(messages("sca-wrapper.fallback.menu.profile"), s"${fallbackPertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
    MenuItemConfig(messages("sca-wrapper.fallback.menu.signout"), s"$fallbackPertaxUrl/signout/feedback/PERTAX", leftAligned = false, position = 3, None, None)
  )

  //fallback wrapper data response in the event that wrapper data is offline
  def fallbackWrapperDataResponse(implicit lang: Lang): WrapperDataResponse = WrapperDataResponse(
    fallbackFeedbackFrontendUrl, fallbackContactUrl, fallbackBusinessTaxAccountUrl,
    fallbackPertaxUrl, fallbackAccessibilityStatementUrl, ggSigninUrl, fallbackMenuConfig
  )
}
