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
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, OnlyRelative, RedirectUrl}
import uk.gov.hmrc.sca.controllers.routes
import uk.gov.hmrc.sca.models.{MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (configuration: Configuration, messages: MessagesApi) {

  //library manual update, MAJOR.MINOR.PATCH
  val versionNum: String = "1.0.3"

  //config for service name in black bar
  val serviceNameKey: Option[String] = configuration.getOptional[String]("sca-wrapper.service-name.messages-key")

  //service name config for links
  val feedbackServiceName: String = configuration.get[String]("sca-wrapper.feedback-service-name")
  private val host: String        = configuration.get[String]("sca-wrapper.host")

  def feedbackUrl(contactFrontendUrl: String)(implicit request: RequestHeader): String = {
    val redirectUrl = RedirectUrl(host + request.uri).getEither(
      OnlyRelative | AbsoluteWithHostnameFromAllowlist("localhost")
    ) match {
      case Right(safeRedirectUrl) => safeRedirectUrl.url
      case Left(error)            => throw new IllegalArgumentException(error)
    }

    s"$contactFrontendUrl?service=$feedbackServiceName&backUrl=$redirectUrl"
  }

  private val accessibilityStatementReferrerUrl =
    configuration.get[String]("sca-wrapper.accessibility-statement.referrer.url")
  private val accessibilityStatementRedirectUrl =
    configuration.get[String]("sca-wrapper.accessibility-statement.redirect.url")

  def accessibilityStatementUrl(accessibilityBaseUrl: String): String = {
    val redirectUrl = RedirectUrl(host + accessibilityStatementReferrerUrl).getEither(
      OnlyRelative | AbsoluteWithHostnameFromAllowlist("localhost")
    ) match {
      case Right(safeRedirectUrl) => safeRedirectUrl.url
      case Left(error)            => throw new IllegalArgumentException(error)
    }
    s"$accessibilityBaseUrl/accessibility-statement/$accessibilityStatementRedirectUrl?referrerUrl=${URLEncoder
      .encode(redirectUrl, "UTF-8")}"
  }

  val enc                              = URLEncoder.encode(_: String, "UTF-8")
  val exitSurveyOrigin: Option[String] = configuration.getOptional[String]("sca-wrapper.exit-survey-origin")

  //service config
  val timeout: Int                   = configuration.get[Int]("sca-wrapper.timeout-dialog.timeout")
  val countdown: Int                 = configuration.get[Int]("sca-wrapper.timeout-dialog.countdown")
  val welshToggle: Boolean           = configuration.get[Boolean]("sca-wrapper.welsh-enabled")
  val disableSessionExpired: Boolean = configuration.get[Boolean]("sca-wrapper.disable-session-expired")

  //signout links
  private val signoutBaseUrl: String            = configuration.get[String]("sca-wrapper.signout.url")
  private val signoutBaseUrlAlt: Option[String] =
    configuration.getOptional[String]("sca-wrapper.signout.alternative-url")
  val signoutUrl: String                        = signoutBaseUrlAlt.getOrElse(signoutBaseUrl)
  val timeOutUrl: Option[String]                = configuration.getOptional[String]("sca-wrapper.signin.url")

  //internal
  val serviceUrl: String   = configuration.get[String]("sca-wrapper.service.url")
  val keepAliveUrl: String = routes.KeepAliveController.keepAlive.url

  //service urls
  val pertaxUrl: String                    = s"${configuration.get[String]("sca-wrapper.services.pertax-frontend.url")}/personal-account"
  val trackingUrl: String                  = s"${configuration.get[String]("sca-wrapper.services.tracking-frontend.url")}"
  val feedbackFrontendUrl: String          =
    s"${configuration.get[String]("sca-wrapper.services.feedback-frontend.url")}/feedback"
  val contactFrontendUrl: String           =
    s"${configuration.get[String]("sca-wrapper.services.contact-frontend.url")}/contact/beta-feedback"
  val accessibilityStatementUrl: String    =
    configuration.get[String]("sca-wrapper.services.accessibility-statement-frontend.url")
  val scaWrapperDataUrl                    =
    s"${configuration.get[String]("sca-wrapper.services.single-customer-account-wrapper-data.url")}/single-customer-account-wrapper-data"
  val helpImproveBannerUrl: Option[String] =
    configuration.getOptional[String]("sca-wrapper.services.help-improve-banner.url")

  // banners
  val showAlphaBanner: Boolean       = configuration.get[Boolean]("sca-wrapper.banners.show-alpha")
  val showBetaBanner: Boolean        = configuration.get[Boolean]("sca-wrapper.banners.show-beta")
  val showHelpImproveBanner: Boolean = configuration.get[Boolean]("sca-wrapper.banners.show-help-improve")

  //fallback menu config in the event that wrapper data is offline
  private def fallbackMenuConfig(implicit lang: Lang): Seq[MenuItemConfig] = Seq(
    MenuItemConfig(
      "home",
      messages("sca-wrapper.fallback.menu.home"),
      s"$pertaxUrl",
      leftAligned = true,
      position = 0,
      Some("hmrc-account-icon hmrc-account-icon--home"),
      None
    ),
    MenuItemConfig(
      "messages",
      messages("sca-wrapper.fallback.menu.messages"),
      s"$pertaxUrl/messages",
      leftAligned = false,
      position = 0,
      None,
      None
    ),
    MenuItemConfig(
      "progress",
      messages("sca-wrapper.fallback.menu.progress"),
      s"$trackingUrl/track",
      leftAligned = false,
      position = 1,
      None,
      None
    ),
    MenuItemConfig(
      "profile",
      messages("sca-wrapper.fallback.menu.profile"),
      s"$pertaxUrl/profile-and-settings",
      leftAligned = false,
      position = 2,
      None,
      None
    ),
    MenuItemConfig(
      "signout",
      messages("sca-wrapper.fallback.menu.signout"),
      s"$signoutUrl",
      leftAligned = false,
      position = 3,
      None,
      None
    )
  )

  //fallback wrapper data response in the event that wrapper data is offline
  def fallbackWrapperDataResponse(implicit lang: Lang): WrapperDataResponse = WrapperDataResponse(
    fallbackMenuConfig,
    PtaMinMenuConfig(
      menuName = messages("sca-wrapper.fallback.menu.name"),
      backName = messages("sca-wrapper.fallback.menu.back")
    )
  )
}
