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

package views

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.sca.models.BannerConfig
import uk.gov.hmrc.sca.views.html.StandardScaLayout
import utils.ViewBaseSpec
import views.NewScaLayoutViewSpec.menu

import scala.jdk.CollectionConverters.CollectionHasAsScala

class StandardScaLayoutViewSpec extends ViewBaseSpec {

  private val standardScaLayout = app.injector.instanceOf[StandardScaLayout]

  private def createView(
    sidebarContent: Option[Html] = None,
    showBackLinkJS: Boolean = false,
    backLinkUrl: Option[String] = None,
    showSignOutInHeader: Boolean = false,
    serviceURLs: ServiceURLs = ServiceURLs(
      serviceUrl = Some("Service-Name_Url"),
      signOutUrl = Some("Signout-Url"),
      accessibilityStatementUrl = Some("http://accessibility-url.org")
    ),
    bannerConfig: BannerConfig =
      BannerConfig(showAlphaBanner = true, showBetaBanner = false, showHelpImproveBanner = false),
    fullWidth: Boolean = false,
    hideMenuBar: Boolean = false,
    disableSessionExpired: Boolean = false,
    optTrustedHelper: Option[TrustedHelper] = None
  )(implicit messages: Messages): Html =
    standardScaLayout(
      if (hideMenuBar) None else menu,
      serviceURLs,
      Some("Service-Name-Key"),
      Some("Page-Title"),
      sidebarContent,
      Some("TimeOut-Url"),
      "Keep-Alive-Url",
      showBackLinkJS,
      backLinkUrl,
      showSignOutInHeader,
      Seq(Html("<script src=/customscript.js></script>")),
      Seq(Html("<link href=/customStylesheet rel=stylesheet/>")),
      bannerConfig,
      fullWidth,
      disableSessionExpired,
      optTrustedHelper,
      Some("test-ur-banner-link")
    )(Html("Content-Block"))(fakeRequest, messages)

  "WrapperService layout" must {
    "return a Wrapper layout with default parameters in English" in {
      val document = asDocument(createView().toString())

      document.title() mustBe "Page-Title"
      document.select(".govuk-header__service-name").attr("href") mustBe "Service-Name_Url"
      document.select(".govuk-header__service-name").text() mustBe "Service-Name-Key"
      document.getElementById("secondary-nav").className mustBe "hmrc-account-menu"
      document.getElementById("secondary-nav").attr("data-module") mustBe "hmrc-account-menu"
      document.getElementById("menu.name").text() mustBe "Account menu"
      document.getElementById("menu.back").text() mustBe "Back"
      document.getElementById("menu.left.0").text() mustBe "Account home"
      document.getElementById("menu.right.0").text() mustBe "Messages"
      document.getElementById("menu.right.1").text() mustBe "Check progress"
      document.getElementById("menu.right.2").text() mustBe "Profile and settings"
      document.getElementById("menu.right.3").text() mustBe "Sign out"
      document.getElementById("menu.left.0").attr("href") mustBe "pertaxUrl"
      document.getElementById("menu.right.0").attr("href") mustBe "pertaxUrl-messages"
      document.getElementById("menu.right.1").attr("href") mustBe "trackingUrl-track"
      document.getElementById("menu.right.2").attr("href") mustBe "pertaxUrl-profile-and-settings"
      document.getElementById("menu.right.3").attr("href") mustBe "pertaxUrl-signout-feedback-PERTAX"
      document.select(".govuk-skip-link").text() mustBe "Skip to main content"
      document
        .getElementsByAttributeValue("name", "hmrc-timeout-dialog")
        .attr("data-keep-alive-url") mustBe "Keep-Alive-Url"
      document.getElementsByAttributeValue("name", "hmrc-timeout-dialog").attr("data-sign-out-url") mustBe "Signout-Url"
      document
        .getElementsByAttributeValue("name", "hmrc-timeout-dialog")
        .attr("data-language")
        .contains("en") mustBe true
      document.getElementsByAttributeValue("name", "hmrc-timeout-dialog").attr("content") mustBe "hmrc-timeout-dialog"
      document.getElementsByAttributeValue("name", "hmrc-timeout-dialog").attr("data-timeout") mustBe "900"
      document.getElementsByAttributeValue("name", "hmrc-timeout-dialog").attr("data-countdown") mustBe "120"
      document.select(".hmrc-language-select__list-item").asScala.exists(e => e.text.equals("English")) mustBe true
      document
        .select(".hmrc-language-select__list-item")
        .asScala
        .exists(e => e.text.equals("Newid yr iaith ir Gymraeg Cymraeg")) mustBe true
      document.getElementsByAttributeValue("href", "/help/cookies").text() mustBe "Cookies"
      document.getElementsByAttributeValue("href", "/help/privacy").text() mustBe "Privacy policy"
      document.getElementsByAttributeValue("href", "/help/terms-and-conditions").text() mustBe "Terms and conditions"
      document.getElementsByAttributeValue("href", "https://www.gov.uk/help").text() mustBe "Help using GOV.UK"
      document
        .getElementsByAttributeValue("href", "https://www.gov.uk/government/organisations/hm-revenue-customs/contact")
        .text() mustBe "Contact"
      document
        .getElementsByAttributeValue("href", "https://www.gov.uk/cymraeg")
        .text() mustBe "Rhestr o Wasanaethau Cymraeg"
      document
        .select(".govuk-footer__licence-description")
        .text() mustBe "All content is available under the Open Government Licence v3.0, except where otherwise stated"
      document.select(".govuk-footer__copyright-logo").text() mustBe "© Crown copyright"
      document
        .select(".govuk-footer__copyright-logo")
        .attr(
          "href"
        ) mustBe "https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/"
      document.getElementsByTag("script").asScala.exists(x => x.attr("src").equals("/customscript.js")) mustBe true
      document.getElementsByTag("link").asScala.exists(x => x.attr("href").equals("/assets/pta.css")) mustBe true
      document.getElementsByTag("link").asScala.exists(x => x.attr("href").equals("/customStylesheet")) mustBe true
      document
        .select(".govuk-phase-banner__content")
        .asScala
        .headOption
        .map(_.text()) mustBe Some("Alpha This is a new service – your feedback will help us to improve it.")
      document.getElementsByTag("h2").asScala.exists(x => x.text().equals("Support links")) mustBe true
      document.select(".govuk-grid-column-two-thirds").asScala.nonEmpty mustBe true
      document
        .getElementsByClass("govuk-link hmrc-user-research-banner__link")
        .text() mustBe "Sign up to take part in research (opens in new tab)"
      document
        .getElementsByClass("govuk-link hmrc-user-research-banner__link")
        .asScala
        .exists(x => x.attr("href").equals("test-ur-banner-link")) mustBe true
    }

    "return a Wrapper layout when there is a sidebar content in English" in {
      val document = asDocument(createView(sidebarContent = Some(Html("Sidebar-Content"))).toString())

      document.select(".govuk-grid-column-one-third").text() mustBe "Sidebar-Content"
    }

    "return a Wrapper layout when there is a backlinkUrl in English" in {
      val document = asDocument(createView(backLinkUrl = Some("backlink-url")).toString())

      document.getElementsByAttributeValue("href", "backlink-url").text() mustBe "Back"
    }

    "return a Wrapper layout when showSignOutInHeader is true in English" in {
      val document = asDocument(
        createView(showSignOutInHeader = true, serviceURLs = ServiceURLs(signOutUrl = Some("Signout-Url"))).toString()
      )

      document
        .getElementsByAttributeValue("data-module", "govuk-header")
        .hasClass("hmrc-header--with-additional-navigation")
      document.select(".hmrc-sign-out-nav__link").attr("href") mustBe "Signout-Url"
      document.select(".hmrc-sign-out-nav__link").text() mustBe "Sign out"
    }

    "return a Wrapper layout when showBetaBanner is true in English" in {
      val document = asDocument(
        createView(bannerConfig =
          BannerConfig(showAlphaBanner = false, showBetaBanner = true, showHelpImproveBanner = false)
        ).toString()
      )

      document
        .select(".govuk-phase-banner__content")
        .asScala
        .exists(x =>
          x.text().equals("Beta This is a new service – your feedback will help us to improve it.")
        ) mustBe true
    }

    "return a Wrapper layout when fullWidth is true in English" in {
      val document = asDocument(createView(fullWidth = true).toString())
      document.select(".govuk-grid-column-full").asScala.nonEmpty mustBe true
    }

    "return a Wrapper layout when hideMenuBar is true in English" in {
      val document = asDocument(createView(hideMenuBar = true).toString())

      document.getElementById("secondary-nav") mustBe null
      document.getElementById("menu.name") mustBe null
      document.getElementById("menu.back") mustBe null
      document.getElementById("menu.left.0") mustBe null
      document.getElementById("menu.right.0") mustBe null
      document.getElementById("menu.right.1") mustBe null
      document.getElementById("menu.right.2") mustBe null
      document.getElementById("menu.right.3") mustBe null
    }

    "return a Wrapper layout when disableSessionExpired is true in English" in {
      val document = asDocument(createView(disableSessionExpired = true).toString())

      document.getElementsByAttributeValue("name", "hmrc-timeout-dialog").isEmpty mustBe true
    }

    "return a Wrapper layout there is a trusted helper in English" in {
      val document = asDocument(
        createView(optTrustedHelper =
          Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", Some("principalNino")))
        ).toString()
      )

      document
        .getElementById("attorneyBanner")
        .html()
        .nonEmpty mustBe true

      document
        .getElementById("attorneyBanner")
        .getElementsByAttribute("href")
        .attr("href") mustBe "returnLinkUrl"
    }

  }
}

object NewScaLayoutViewSpec {
  val menu: Option[Html] = Some(
    Html(
      "\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\" class=\"hmrc-account-menu__link \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Account menu\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-signout-feedback-PERTAX\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n"
    )
  )
}
