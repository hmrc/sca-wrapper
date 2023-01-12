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
import uk.gov.hmrc.sca.models.MenuItemConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val xxx = configuration.get[String]("test1")
  private val scaWrapperDataBaseUrl = configuration.get[String]("microservice.services.single-customer-account-wrapper-data.url")
  val scaWrapperDataUrl = s"$scaWrapperDataBaseUrl/single-customer-account-wrapper-data"

  val wrapperDataTimeout: Duration = Duration(configuration.get[String]("wrapper-data-timeout"))

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")
  private val exitSurveyBaseUrl: String = configuration.get[String]("microservice.services.feedback-frontend.url")
  def exitSurveyUrl(serviceName: String): String = s"$exitSurveyBaseUrl/feedback/$serviceName"

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  private val appName: String = configuration.get[String]("appName")
  private val host: String = configuration.get[String]("host")
  private val contactBaseUrl: String = configuration.get[String]("microservice.services.contact-frontend.url")
  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactBaseUrl/contact/beta-feedback?service=$appName&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"


  val pertaxUrl: String = s"${configuration.get[String]("microservice.services.pertax-frontend.url")}/personal-account"
  val businessTaxAccountUrl: String = s"${configuration.get[String]("microservice.services.business-tax-frontend.url")}/business-tax-account"

  private def signoutParams(continueUrl: Option[String], origin: Option[String]) = {
    val contUrl = s"${continueUrl.fold("") { url => s"continueUrl=$url" }}"
    val originUrl = s"${origin.fold("") { url => s"origin=$url" }}"
    (contUrl, originUrl) match {
      case _ if contUrl.nonEmpty && origin.nonEmpty => s"?$contUrl&$originUrl"
      case _ if contUrl.isEmpty && origin.isEmpty => ""
      case x@_ => s"?${x._1}${x._2}"
    }
  }

  val fallbackMenuConfig: Seq[MenuItemConfig] = Seq(
    MenuItemConfig("Account Home", s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    MenuItemConfig("Messages", s"${pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
    MenuItemConfig("Check progress", s"${pertaxUrl}/track", leftAligned = false, position = 1, None, None),
    MenuItemConfig("Profile and settings", s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
    MenuItemConfig("Business tax account", s"${businessTaxAccountUrl}/business-account", leftAligned = false, position = 3, None, None),
    MenuItemConfig("Sign out", s"${pertaxUrl}/signout${signoutParams(Some("/feedback/PERTAX"), None)}", leftAligned = false, position = 4, None, None)
  )
}
