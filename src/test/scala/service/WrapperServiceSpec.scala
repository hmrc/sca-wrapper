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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc.{AnyContentAsEmpty, Cookie, Cookies}
import play.api.test.FakeRequest
import play.api.test.Helpers.baseApplicationBuilder.injector
import play.api.test.Helpers.stubMessages
import play.api.{Application, inject}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{BannerConfig, MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.utils.Keys
import uk.gov.hmrc.sca.views.html.{NewScaLayout, PtaMenuBar, ScaLayout}

class WrapperServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import WrapperServiceSpec._

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val messages: Messages = stubMessages()

  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withAttrs(TypedMap(
      Keys.wrapperDataKey -> wrapperDataResponse,
      Keys.messageDataKey -> Some(2),
      RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en")))))
    )

  private val mockScaLayout = mock[ScaLayout]
  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]
  private val mockAppConfig = mock[AppConfig]
  private val mockPtaMenuBar = mock[PtaMenuBar]
  private val mockNewScaLayout = mock[NewScaLayout]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[ScaLayout].toInstance(mockScaLayout),
      inject.bind[NewScaLayout].toInstance(mockNewScaLayout),
      inject.bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector),
      inject.bind[AppConfig].toInstance(mockAppConfig)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private def wrapperService = application.injector.instanceOf[WrapperService]

  override def beforeEach(): Unit = {
    reset(mockScaLayout)
    reset(mockNewScaLayout)
    reset(mockScaWrapperDataConnector)
    reset(mockAppConfig)
  }

  "WrapperService" must {

    "return default layout" in {

      when(mockAppConfig.showAlphaBanner).thenReturn(true)
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.signoutUrl).thenReturn("Signout-Url")
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)


      wrapperService.layout(content = Html("Default-Content"))

      verify(mockScaLayout, times(1)).apply(menu = menuCaptor.capture(), serviceNameKey = serviceNameKeyCaptor.capture(),
        serviceNameUrl = serviceNameUrlCaptor.capture(), pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(), signoutUrl = signoutUrlCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(), keepAliveUrl = keepAliveUrlCaptor.capture(), showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(), showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(), styleSheets = styleSheetsCaptor.capture(), bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(), hideMenuBar = hideMenuBarCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture())(contentCaptor.capture())(any(), any(), any())

      verify(mockAppConfig, times(1)).showAlphaBanner
      verify(mockAppConfig, times(1)).showBetaBanner
      verify(mockAppConfig, times(1)).showHelpImproveBanner
      verify(mockAppConfig, times(1)).serviceNameKey
      verify(mockAppConfig, times(1)).signoutUrl
      verify(mockAppConfig, times(1)).keepAliveUrl
      verify(mockAppConfig, times(1)).disableSessionExpired

      menuCaptor.getValue mustBe menu
      serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
      serviceNameUrlCaptor.getValue mustBe None
      pageTitleCaptor.getValue mustBe None
      sideBarContentCaptor.getValue mustBe None
      signoutUrlCaptor.getValue mustBe "Signout-Url"
      keepAliveUrlCaptor.getValue mustBe "/refresh-session"
      showBackLinkJSCaptor.getValue mustBe false
      backLinkUrlCaptor.getValue mustBe None
      showSignOutInHeaderCaptor.getValue mustBe false
      scriptsCaptor.getValue mustBe Seq.empty
      styleSheetsCaptor.getValue mustBe Seq.empty
      bannerConfigCaptor.getValue mustBe BannerConfig(showAlphaBanner = true, showBetaBanner = false, showHelpImproveBanner = true)
      optTrustedHelperCaptor.getValue mustBe None
      fullWidthCaptor.getValue mustBe true
      hideMenuBarCaptor.getValue mustBe false
      disableSessionExpiredCaptor.getValue mustBe false
      contentCaptor.getValue mustBe Html("Default-Content")
    }

    "return default New Sca layout" in {

      when(mockAppConfig.showAlphaBanner).thenReturn(true)
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.signoutUrl).thenReturn("Signout-Url")
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)


      wrapperService.newLayout(content = Html("Default-Content"))

      verify(mockNewScaLayout, times(1)).apply(menu = menuCaptor.capture(), serviceNameKey = serviceNameKeyCaptor.capture(),
        serviceNameUrl = serviceNameUrlCaptor.capture(), pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(), signoutUrl = signoutUrlCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(), keepAliveUrl = keepAliveUrlCaptor.capture(), showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(), showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(), styleSheets = styleSheetsCaptor.capture(), bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(), hideMenuBar = hideMenuBarCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture())(contentCaptor.capture())(any(), any(), any())

      verify(mockAppConfig, times(1)).showAlphaBanner
      verify(mockAppConfig, times(1)).showBetaBanner
      verify(mockAppConfig, times(1)).showHelpImproveBanner
      verify(mockAppConfig, times(1)).serviceNameKey
      verify(mockAppConfig, times(1)).signoutUrl
      verify(mockAppConfig, times(1)).keepAliveUrl
      verify(mockAppConfig, times(1)).disableSessionExpired

      menuCaptor.getValue mustBe menu
      serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
      serviceNameUrlCaptor.getValue mustBe None
      pageTitleCaptor.getValue mustBe None
      sideBarContentCaptor.getValue mustBe None
      signoutUrlCaptor.getValue mustBe "Signout-Url"
      keepAliveUrlCaptor.getValue mustBe "/refresh-session"
      showBackLinkJSCaptor.getValue mustBe false
      backLinkUrlCaptor.getValue mustBe None
      showSignOutInHeaderCaptor.getValue mustBe false
      scriptsCaptor.getValue mustBe Seq.empty
      styleSheetsCaptor.getValue mustBe Seq.empty
      bannerConfigCaptor.getValue mustBe BannerConfig(showAlphaBanner = true, showBetaBanner = false, showHelpImproveBanner = true)
      optTrustedHelperCaptor.getValue mustBe None
      fullWidthCaptor.getValue mustBe true
      hideMenuBarCaptor.getValue mustBe false
      disableSessionExpiredCaptor.getValue mustBe false
      contentCaptor.getValue mustBe Html("Default-Content")
    }

    "return layout" in {

      val content = Html("Content")
      val pageTitle = Some("Page-Title")
      val serviceNameKey = Some("Service-Name-Key")
      val serviceNameUrl = Some("Service-Name-Url")
      val sidebarContent = Some(Html("Sidebar-Content"))
      val signoutUrl = "Signout-Url"
      val timeOutUrl = Some("Timeout-Url")
      val keepAliveUrl = "Keep-Alive-Url"
      val showBackLinkJS = true
      val backLinkUrl = Some("Backlink-Url")
      val showSignOutInHeader = true
      val scripts = Seq(Html("Scripts"))
      val styleSheets = Seq(Html("StyleSheets"))
      val bannerConfig = BannerConfig(showAlphaBanner = false, showBetaBanner = true, showHelpImproveBanner = false)
      val optTrustedHelper = Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", "principalNino"))
      val fullWidth = true
      val hideMenuBar = true
      val disableSessionExpired = true

      wrapperService.layout(content, pageTitle, serviceNameKey, serviceNameUrl, sidebarContent,
        signoutUrl, timeOutUrl, keepAliveUrl, showBackLinkJS, backLinkUrl, showSignOutInHeader,
        scripts, styleSheets, bannerConfig, optTrustedHelper, fullWidth, hideMenuBar, disableSessionExpired)

      verify(mockScaLayout, times(1)).apply(menu = menuCaptor.capture(), serviceNameKey = serviceNameKeyCaptor.capture(),
        serviceNameUrl = serviceNameUrlCaptor.capture(), pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(), signoutUrl = signoutUrlCaptor.capture(), timeOutUrl = timeOutUrlCaptor.capture(),
        keepAliveUrl = keepAliveUrlCaptor.capture(), showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(), showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(), styleSheets = styleSheetsCaptor.capture(), bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(), hideMenuBar = hideMenuBarCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture())(contentCaptor.capture())(any(), any(), any())

      verify(mockAppConfig, never).showAlphaBanner
      verify(mockAppConfig, never).showBetaBanner
      verify(mockAppConfig, never).showHelpImproveBanner
      verify(mockAppConfig, never).serviceNameKey
      verify(mockAppConfig, never).signoutUrl
      verify(mockAppConfig, never).keepAliveUrl
      verify(mockAppConfig, never).disableSessionExpired

      menuCaptor.getValue mustBe menu
      serviceNameKeyCaptor.getValue mustBe serviceNameKey
      serviceNameUrlCaptor.getValue mustBe serviceNameUrl
      pageTitleCaptor.getValue mustBe pageTitle
      sideBarContentCaptor.getValue mustBe sidebarContent
      signoutUrlCaptor.getValue mustBe signoutUrl
      timeOutUrlCaptor.getValue mustBe timeOutUrl
      keepAliveUrlCaptor.getValue mustBe keepAliveUrl
      showBackLinkJSCaptor.getValue mustBe showBackLinkJS
      backLinkUrlCaptor.getValue mustBe backLinkUrl
      showSignOutInHeaderCaptor.getValue mustBe showSignOutInHeader
      scriptsCaptor.getValue mustBe scripts
      styleSheetsCaptor.getValue mustBe styleSheets
      bannerConfigCaptor.getValue mustBe bannerConfig
      optTrustedHelperCaptor.getValue mustBe optTrustedHelper
      fullWidthCaptor.getValue mustBe fullWidth
      hideMenuBarCaptor.getValue mustBe hideMenuBar
      disableSessionExpiredCaptor.getValue mustBe disableSessionExpired
      contentCaptor.getValue mustBe content
    }

    "return the continueUrl if it is a valid relative URL" in {
      val continueUrl = Some(RedirectUrl("/url"))

      val actualUrl = wrapperService.safeSignoutUrl(continueUrl)

      actualUrl mustBe Some("/url")
    }

    "return the exitSurveyOrigin if the continueUrl is None" in {
      val appConfig: AppConfig = injector.instanceOf[AppConfig]

      val expectedUrl = appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))

      val response = new WrapperService(mockPtaMenuBar, mockScaLayout, mockNewScaLayout, appConfig).safeSignoutUrl(None)

      response mustBe expectedUrl
    }

  }
}

object WrapperServiceSpec {

  val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
  val menuItemConfig1: MenuItemConfig = MenuItemConfig("home", "Account home", "pertaxUrl", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None)
  val menuItemConfig2: MenuItemConfig = MenuItemConfig("messages", "Messages", "pertaxUrl-messages", leftAligned = false, position = 0, None, None)
  val menuItemConfig3: MenuItemConfig = MenuItemConfig("progress", "Check progress", "trackingUrl-track", leftAligned = false, position = 1, None, None)
  val menuItemConfig4: MenuItemConfig = MenuItemConfig("profile", "Profile and settings", "pertaxUrl-profile-and-settings", leftAligned = false, position = 2, None, None)
  val menuItemConfig5: MenuItemConfig = MenuItemConfig("signout", "Sign out", "pertaxUrl-signout-feedback-PERTAX", leftAligned = false, position = 3, None, None)
  val menu: Html = Html("\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\"\n   class=\"hmrc-account-menu__link hmrc-account-menu__link--home\n   \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Account menu\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n    <span class=\"hmrc-notification-badge\">2</span>\n\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"Signout-Url\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n")

  private val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5), ptaMenuConfig)

  val menuCaptor = ArgumentCaptor.forClass(classOf[Html])
  val serviceNameKeyCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val serviceNameUrlCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val pageTitleCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val sideBarContentCaptor = ArgumentCaptor.forClass(classOf[Option[Html]])
  val signoutUrlCaptor = ArgumentCaptor.forClass(classOf[String])
  val timeOutUrlCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val keepAliveUrlCaptor = ArgumentCaptor.forClass(classOf[String])
  val showBackLinkJSCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val backLinkUrlCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val showSignOutInHeaderCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val scriptsCaptor = ArgumentCaptor.forClass(classOf[Seq[HtmlFormat.Appendable]])
  val styleSheetsCaptor = ArgumentCaptor.forClass(classOf[Seq[HtmlFormat.Appendable]])
  val bannerConfigCaptor = ArgumentCaptor.forClass(classOf[BannerConfig])
  val optTrustedHelperCaptor = ArgumentCaptor.forClass(classOf[Option[TrustedHelper]])
  val fullWidthCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val hideMenuBarCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val disableSessionExpiredCaptor = ArgumentCaptor.forClass(classOf[Boolean])
  val contentCaptor = ArgumentCaptor.forClass(classOf[Html])
}
