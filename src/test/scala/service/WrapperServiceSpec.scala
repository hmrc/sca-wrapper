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

package service

import fixtures.BaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.Lang
import play.api.test.Helpers
import play.twirl.api.Html
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, ScaLayout}

import scala.concurrent.Future

class WrapperServiceSpec extends BaseSpec {

  private val connector = mock[ScaWrapperDataConnector]
  private val menu = app.injector.instanceOf[PtaMenuBar]
  private val layout = app.injector.instanceOf[ScaLayout]


  private val service = new WrapperService(Helpers.stubMessagesControllerComponents(), menu, layout, connector, appConfig)
  private val scripts: Option[Html]  = Option(Html("<scripts>ptaScript</scripts>"))
  "WrapperService layout" must {
    "return a Wrapper layout in English" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(),any(),any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts
      )

      whenReady(result) { res =>
        res.body must include("test.test")
        res.body must include("Account menu")
        res.body must include("Back")
        res.body must include("Account home")
        res.body must include("Messages")
        res.body must include("Check progress")
        res.body must include("Profile and settings")
        res.body must include("Sign out")
        res.body must include("href=\"http://localhost:9232/personal-account/signout/feedback/PERTAX\"")
        res.body must include("href=\"http://localhost:9232/personal-account/profile-and-settings\"")
        res.body must include("href=\"http://localhost:9100/track\"")
        res.body must include("href=\"http://localhost:9232/personal-account/messages\"")
        res.body must include("href=\"http://localhost:9232/personal-account\"")
        res.body must include("Skip to main content")
        res.body must include("/refresh-session")
        res.body must include("data-sign-out-url=\"http://localhost:9232/personal-account/signout\"")
        res.body must include("data-language=\"en\"")
        res.body must include("content=\"hmrc-timeout-dialog\"")
        res.body must include("English")
        res.body must include("Cymraeg")
        res.body must include("href=\"http://localhost:12346/accessibility-statement/single-customer-account-frontend?referrerUrl=http%3A%2F%2Flocalhost%3A12346%2Fsingle-customer-account\"")
        res.body must include("Cookies")
        res.body must include("Accessibility statement")
        res.body must include("Privacy policy")
        res.body must include("Terms and conditions")
        res.body must include("Help using GOV.UK")
        res.body must include("Contact")
        res.body must include("Rhestr o Wasanaethau Cymraeg")
        res.body must include("All content is available under the")
        res.body must include("Open Government Licence v3.0</a>, except where otherwise stated")
        res.body must include("Crown copyright")
        res.body must include("ptaScript")
        res.body mustNot include("alpha")
        res.body mustNot include("beta")
        res.body mustNot include("This is a new service – your <a class=\"govuk-link\" href=\"http://localhost:9250/contact/beta-feedback?service=single-customer-account-frontend&amp;backUrl=http%3A%2F%2Flocalhost%3A9000\">feedback</a> will help us to improve it.")
      }
    }

    "return a Wrapper layout in Welsh" in {
      implicit val lang: Lang = Lang("cy")
      when(connector.wrapperData(any())(any(),any(),any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test")
      )(messages = messagesCy, hc = hc, request = fakeRequest)

      whenReady(result) { res =>
        res.body must include("test.test")
        res.body must include("Hafan y cyfrif")
        res.body must include("Negeseuon")
        res.body must include("Gwirio cynnydd")
        res.body must include("Proffil a gosodiadau")
        res.body must include("Allgofnodi")
        res.body must include("href=\"http://localhost:9232/personal-account/signout/feedback/PERTAX\"")
        res.body must include("href=\"http://localhost:9232/personal-account/profile-and-settings\"")
        res.body must include("href=\"http://localhost:9100/track\"")
        res.body must include("href=\"http://localhost:9232/personal-account/messages\"")
        res.body must include("href=\"http://localhost:9232/personal-account\"")
        res.body must include("Ewch yn syth i‘r prif gynnwys")
        res.body must include("/refresh-session")
        res.body must include("data-sign-out-url=\"http://localhost:9232/personal-account/signout\"")
        res.body must include("data-language=\"cy\"")
        res.body must include("content=\"hmrc-timeout-dialog\"")
        res.body must include("English")
        res.body must include("Cymraeg")
        res.body must include("href=\"http://localhost:12346/accessibility-statement/single-customer-account-frontend?referrerUrl=http%3A%2F%2Flocalhost%3A12346%2Fsingle-customer-account\"")
        res.body must include("Cwcis")
        res.body must include("Datganiad hygyrchedd")
        res.body must include("Polisi preifatrwydd")
        res.body must include("Telerau ac Amodau")
        res.body must include("Help wrth ddefnyddio GOV.UK")
        res.body must include("Cysylltu")
        res.body must include("Rhestr o Wasanaethau Cymraeg")
        res.body must include("Mae‘r holl gynnwys ar gael o dan")
        res.body must include("Drwydded Llywodraeth Agored v3.0</a>, oni nodir yn wahanol")
        res.body must include("Hawlfraint y Goron")
        res.body mustNot include("alpha")
        res.body mustNot include("beta")
        res.body mustNot include("Gwasanaeth newydd yw hwn – bydd eich")
      }
    }

    "show the Alpha banner if selected" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(), any(), any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts,
        showAlphaBanner = true
      )

      whenReady(result) { res =>
        res.body must include("alpha")
        res.body must include("This is a new service – your <a class=\"govuk-link\" href=\"http://localhost:9250/contact/beta-feedback?service=single-customer-account-frontend&amp;backUrl=http%3A%2F%2Flocalhost%3A9000\">feedback</a> will help us to improve it.")
      }
    }

    "show the Beta banner if selected" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(), any(), any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts,
        showBetaBanner = true
      )

      whenReady(result) { res =>
        res.body mustNot include("alpha")
        res.body must include("beta")
        res.body must include("This is a new service – your <a class=\"govuk-link\" href=\"http://localhost:9250/contact/beta-feedback?service=single-customer-account-frontend&amp;backUrl=http%3A%2F%2Flocalhost%3A9000\">feedback</a> will help us to improve it.")
      }
    }

    "show the help improve banner if  selected" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(), any(), any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts,
        showHelpImproveBanner = true
      )

      whenReady(result) { res =>
        res.body must include("Help improve GOV.UK")
      }
    }

    "do not show the help improve banner if not selected" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(), any(), any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts,
        showHelpImproveBanner = false
      )

      whenReady(result) { res =>
        res.body mustNot include("Help improve GOV.UK")
      }
    }

    "show the child benefit banner if  selected" in {
      implicit val lang: Lang = Lang("en")
      when(connector.wrapperData(any())(any(), any(), any())).thenReturn(Future.successful(appConfig.fallbackWrapperDataResponse))

      val result = service.layout(
        content = Html(""),
        serviceNameKey = Some("test.test"),
        scripts = scripts,
        showChildBenefitBanner = true
      )

      whenReady(result) { res =>
        res.body must include("Help improve the Child Benefit service")
      }
    }
  }
}
