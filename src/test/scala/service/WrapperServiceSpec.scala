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
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import play.api.{Application, inject}
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models.{BannerConfig, MenuItemConfig, PtaMinMenuConfig, WrapperDataResponse}
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.views.html.ScaLayout

import scala.concurrent.Future

class WrapperServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import WrapperServiceSpec._

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val messages: Messages = stubMessages()
  private implicit val request: Request[AnyContent] = FakeRequest()

  private val mockScaLayout = mock[ScaLayout]
  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]
  private val mockAppConfig = mock[AppConfig]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[ScaLayout].toInstance(mockScaLayout),
      inject.bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector),
      inject.bind[AppConfig].toInstance(mockAppConfig)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private def wrapperService = application.injector.instanceOf[WrapperService]

  override def beforeEach(): Unit = {
    reset(mockScaLayout)
    reset(mockScaWrapperDataConnector)
    reset(mockAppConfig)

    when(mockScaWrapperDataConnector.wrapperData(any())(any(), any(), any()))
      .thenReturn(Future.successful(WrapperDataResponse(Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5), ptaMenuConfig)))

  }

  "WrapperService" must {

    "return default layout" in {
      when(mockAppConfig.showAlphaBanner).thenReturn(true)
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.showChildBenefitBanner).thenReturn(false)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.signoutUrl).thenReturn("http://localhost:9232/personal-account/signout")
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)

      wrapperService.layout(content = Html("Default-Content")).map { _ =>
        verify(mockScaLayout, times(1)).apply(menu = menuCaptor.capture(), serviceNameKey = serviceNameKeyCaptor.capture(),
          serviceNameUrl = serviceNameUrlCaptor.capture(), pageTitle = pageTitleCaptor.capture(),
          sidebarContent = sideBarContentCaptor.capture(), signoutUrl = signoutUrlCaptor.capture(),
          keepAliveUrl = keepAliveUrlCaptor.capture(), showBackLinkJS = showBackLinkJSCaptor.capture(),
          backLinkUrl = backLinkUrlCaptor.capture(), showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
          scripts = scriptsCaptor.capture(), styleSheets = styleSheetsCaptor.capture(), bannerConfig = bannerConfigCaptor.capture(),
          fullWidth = fullWidthCaptor.capture(), hideMenuBar = hideMenuBarCaptor.capture(),
          disableSessionExpired = disableSessionExpiredCaptor.capture(),
          optTrustedHelper = optTrustedHelperCaptor.capture())(contentCaptor.capture())(any(), any(), any())


        verify(mockAppConfig, times(1)).showAlphaBanner
        verify(mockAppConfig, times(1)).showBetaBanner
        verify(mockAppConfig, times(1)).showHelpImproveBanner
        verify(mockAppConfig, times(1)).showChildBenefitBanner
        verify(mockAppConfig, times(1)).serviceNameKey
        verify(mockAppConfig, times(1)).signoutUrl
        verify(mockAppConfig, times(1)).keepAliveUrl
        verify(mockAppConfig, times(1)).disableSessionExpired
        verify(mockScaWrapperDataConnector, times(1)).wrapperData(anyString())(any(), any(), any())

        menuCaptor.getValue mustBe Html("\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\" class=\"hmrc-account-menu__link \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Menu Name\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back Name\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-signout-feedback-PERTAX\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n")
        serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
        serviceNameUrlCaptor.getValue mustBe None
        pageTitleCaptor.getValue mustBe None
        sideBarContentCaptor.getValue mustBe None
        signoutUrlCaptor.getValue mustBe "http://localhost:9232/personal-account/signout"
        keepAliveUrlCaptor.getValue mustBe "/refresh-session"
        showBackLinkJSCaptor.getValue mustBe false
        backLinkUrlCaptor.getValue mustBe None
        showSignOutInHeaderCaptor.getValue mustBe false
        scriptsCaptor.getValue mustBe Seq.empty
        styleSheetsCaptor.getValue mustBe Seq.empty
        bannerConfigCaptor.getValue mustBe BannerConfig(showChildBenefitBanner = false, showAlphaBanner = true, showBetaBanner = false, showHelpImproveBanner = true)
        optTrustedHelperCaptor.getValue mustBe None
        fullWidthCaptor.getValue mustBe false
        hideMenuBarCaptor.getValue mustBe false
        disableSessionExpiredCaptor.getValue mustBe false
        contentCaptor.getValue mustBe Html("Default-Content")
      }
    }

    "return layout" in {

      val content = Html("Content")
      val pageTitle = Some("Page-Title")
      val serviceNameKey = Some("Service-Name-Key")
      val serviceNameUrl = Some("Service-Name-Url")
      val sidebarContent = Some(Html("Sidebar-Content"))
      val signoutUrl = "Signout-Url"
      val keepAliveUrl = "Keep-Alive-Url"
      val showBackLinkJS = true
      val backLinkUrl = Some("Backlink-Url")
      val showSignOutInHeader = true
      val scripts = Seq(Html("Scripts"))
      val styleSheets = Seq(Html("StyleSheets"))
      val bannerConfig = BannerConfig(showChildBenefitBanner = true, showAlphaBanner = false, showBetaBanner = true, showHelpImproveBanner = false)
      val optTrustedHelper = Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", "principalNino"))
      val fullWidth = true
      val hideMenuBar = true
      val disableSessionExpired = true

      wrapperService.layout(content, pageTitle, serviceNameKey, serviceNameUrl, sidebarContent,
        signoutUrl, keepAliveUrl, showBackLinkJS, backLinkUrl, showSignOutInHeader,
        scripts, styleSheets, bannerConfig, optTrustedHelper, fullWidth, hideMenuBar, disableSessionExpired).map { _ =>
        verify(mockScaLayout, times(1)).apply(menu = menuCaptor.capture(), serviceNameKey = serviceNameKeyCaptor.capture(),
          serviceNameUrl = serviceNameUrlCaptor.capture(), pageTitle = pageTitleCaptor.capture(),
          sidebarContent = sideBarContentCaptor.capture(), signoutUrl = signoutUrlCaptor.capture(),
          keepAliveUrl = keepAliveUrlCaptor.capture(), showBackLinkJS = showBackLinkJSCaptor.capture(),
          backLinkUrl = backLinkUrlCaptor.capture(), showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
          scripts = scriptsCaptor.capture(), styleSheets = styleSheetsCaptor.capture(), bannerConfig = bannerConfigCaptor.capture(),
          fullWidth = fullWidthCaptor.capture(), hideMenuBar = hideMenuBarCaptor.capture(),
          disableSessionExpired = disableSessionExpiredCaptor.capture(),
          optTrustedHelper = optTrustedHelperCaptor.capture())(contentCaptor.capture())(any(), any(), any())


        verify(mockAppConfig, never).showAlphaBanner
        verify(mockAppConfig, never).showBetaBanner
        verify(mockAppConfig, never).showHelpImproveBanner
        verify(mockAppConfig, never).showChildBenefitBanner
        verify(mockAppConfig, never).serviceNameKey
        verify(mockAppConfig, never).signoutUrl
        verify(mockAppConfig, never).keepAliveUrl
        verify(mockAppConfig, never).disableSessionExpired
        verify(mockScaWrapperDataConnector, times(1)).wrapperData(anyString())(any(), any(), any())

        menuCaptor.getValue mustBe Html("\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\" class=\"hmrc-account-menu__link \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Menu Name\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back Name\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-signout-feedback-PERTAX\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n")
        serviceNameKeyCaptor.getValue mustBe serviceNameKey
        serviceNameUrlCaptor.getValue mustBe serviceNameUrl
        pageTitleCaptor.getValue mustBe pageTitle
        sideBarContentCaptor.getValue mustBe sidebarContent
        signoutUrlCaptor.getValue mustBe signoutUrl
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
    }
  }
}

object WrapperServiceSpec {

  val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Menu Name", backName = "Back Name")
  val menuItemConfig1: MenuItemConfig = MenuItemConfig("Account home", "pertaxUrl", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None)
  val menuItemConfig2: MenuItemConfig = MenuItemConfig("Messages", "pertaxUrl-messages", leftAligned = false, position = 0, None, None)
  val menuItemConfig3: MenuItemConfig = MenuItemConfig("Check progress", "trackingUrl-track", leftAligned = false, position = 1, None, None)
  val menuItemConfig4: MenuItemConfig = MenuItemConfig("Profile and settings", "pertaxUrl-profile-and-settings", leftAligned = false, position = 2, None, None)
  val menuItemConfig5: MenuItemConfig = MenuItemConfig("Sign out", "pertaxUrl-signout-feedback-PERTAX", leftAligned = false, position = 3, None, None)

  val menuCaptor = ArgumentCaptor.forClass(classOf[Html])
  val serviceNameKeyCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val serviceNameUrlCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val pageTitleCaptor = ArgumentCaptor.forClass(classOf[Option[String]])
  val sideBarContentCaptor = ArgumentCaptor.forClass(classOf[Option[Html]])
  val signoutUrlCaptor = ArgumentCaptor.forClass(classOf[String])
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