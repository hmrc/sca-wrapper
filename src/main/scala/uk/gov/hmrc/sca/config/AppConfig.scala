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

  private val appName: String = configuration.get[String]("appName")
  private val host: String = configuration.get[String]("host")

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")

  //wrapper specific
  val xxx = configuration.get[String]("test1")
  private val scaWrapperDataBaseUrl = configuration.get[String]("sca-wrapper.single-customer-account-wrapper-data.url")
  val scaWrapperDataUrl = s"$scaWrapperDataBaseUrl/single-customer-account-wrapper-data"
  private val pertaxBaseUrl: String = configuration.get[String]("sca-wrapper.pertax-frontend.base-url")
  private val pertaxServicePath: String = configuration.get[String]("sca-wrapper.pertax-frontend.service-path")
  private val pertaxSignoutPath: String = configuration.get[String]("sca-wrapper.pertax-frontend.service-path")
  val pertaxUrl = s"$pertaxBaseUrl/$pertaxServicePath"
  val businessTaxAccountBaseUrl: String = configuration.get[String]("sca-wrapper.business-tax-frontend.base-url")
  val businessTaxAccountServicePath: String = configuration.get[String]("sca-wrapper.business-tax-frontend.service-path")
  val businessTaxAccountUrl: String = s"$businessTaxAccountBaseUrl/$businessTaxAccountServicePath"
  val wrapperDataTimeout: Duration = Duration(configuration.get[String]("sca-wrapper.wrapper-data-timeout"))
  private val exitSurveyBaseUrl: String = configuration.get[String]("sca-wrapper.feedback-frontend.url")
  private val exitSurveyServiceName: String = configuration.get[String]("sca-wrapper.exit-survey-service-name")
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/$exitSurveyServiceName"
  private val contactBaseUrl: String = configuration.get[String]("sca-wrapper.contact-frontend.url")
  private val feedbackServiceName: String = configuration.get[String]("sca-wrapper.feedback-service-name")
  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactBaseUrl/contact/beta-feedback?service=$feedbackServiceName&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"
  val timeout: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("sca-wrapper.timeout-dialog.countdown")
  val welshToggle: Boolean = configuration.get[Boolean]("sca-wrapper.welsh-enabled")
  private val usePertaxSignout: Boolean = configuration.get[Boolean]("sca-wrapper.signout.use-pertax-signout")
  private val customSignoutUrl: Option[String] = configuration.get[Option[String]]("sca-wrapper.signout.custom-signout-url ")
  private val continueUrl: Option[String] = configuration.get[Option[String]]("sca-wrapper.signout.continue-url")
  private val origin: Option[String] = configuration.get[Option[String]]("sca-wrapper.signout.origin")
//TODO accessibility
  //wrapper specific

  private def signoutUrl(pertaxSignout: Boolean, customSignout: Option[String]): String = {
    val pertaxSignoutUrl = s"$pertaxUrl/$pertaxSignoutPath"
    if(pertaxSignout){
      pertaxSignoutUrl
    } else {
      //TODO url validation
      customSignout match {
        case Some(url) if url.nonEmpty => customSignout.get
        case _ => pertaxSignoutUrl
      }
    }
  }
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
    MenuItemConfig("Sign out", s"${signoutUrl(usePertaxSignout, customSignoutUrl)}${signoutParams(continueUrl, origin)}", leftAligned = false, position = 4, None, None)
  )
}
