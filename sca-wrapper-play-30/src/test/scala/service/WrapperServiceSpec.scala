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
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{Cell, RequestAttrKey}
import play.api.mvc.{AnyContentAsEmpty, Cookie, Cookies}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.hmrcstandardpage.ServiceURLs
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.connectors.ScaWrapperDataConnector
import uk.gov.hmrc.sca.models._
import uk.gov.hmrc.sca.services.WrapperService
import uk.gov.hmrc.sca.utils.{Keys, WebchatUtil}
import uk.gov.hmrc.sca.views.html.{PtaMenuBar, ScaLayout, StandardScaLayout}
import utils.BaseSpec

import java.net.URLEncoder

class WrapperServiceSpec extends BaseSpec {

  import WrapperServiceSpec._

  private implicit val messages: Messages = stubMessages()

  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withAttrs(
      TypedMap(
        Keys.wrapperDataKey    -> wrapperDataResponse,
        Keys.messageDataKey    -> 2,
        RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
      )
    )

  private val mockScaLayout               = mock[ScaLayout]
  private val mockScaWrapperDataConnector = mock[ScaWrapperDataConnector]
  private val mockAppConfig               = mock[AppConfig]
  private val mockPtaMenuBar              = mock[PtaMenuBar]
  private val mockStandardScaLayout       = mock[StandardScaLayout]
  private val mockWebchatUtil             = mock[WebchatUtil]

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[ScaLayout].toInstance(mockScaLayout),
      bind[StandardScaLayout].toInstance(mockStandardScaLayout),
      bind[ScaWrapperDataConnector].toInstance(mockScaWrapperDataConnector),
      bind[AppConfig].toInstance(mockAppConfig),
      bind[WebchatUtil].toInstance(mockWebchatUtil)
    )

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .overrides(modules: _*)
    .build()

  private def wrapperService = app.injector.instanceOf[WrapperService]

  private val serviceUrls = ServiceURLs(
    serviceUrl = None,
    signOutUrl = Some("Signout-Url"),
    accessibilityStatementUrl = Some(null)
  )

  override def beforeEach(): Unit = {
    reset(mockScaLayout)
    reset(mockStandardScaLayout)
    reset(mockScaWrapperDataConnector)
    reset(mockAppConfig)
    reset(mockWebchatUtil)
  }

  "WrapperService" must {

    "return default layout" in {

      when(mockAppConfig.showAlphaBanner).thenReturn(true)
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)
      when(mockWebchatUtil.getWebchatScripts(any())).thenReturn(Seq.empty[Html])

      wrapperService.layout(signoutUrl = Some("Signout-Url"), content = Html("Default-Content"))

      verify(mockScaLayout, times(1)).apply(
        menu = menuCaptor.capture(),
        serviceNameKey = serviceNameKeyCaptor.capture(),
        serviceNameUrl = serviceNameUrlCaptor.capture(),
        pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(),
        signoutUrl = signoutUrlCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(),
        keepAliveUrl = keepAliveUrlCaptor.capture(),
        showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(),
        showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(),
        styleSheets = styleSheetsCaptor.capture(),
        bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture(),
        accessibilityStatementUrl = any()
      )(contentCaptor.capture())(any(), any())

      verify(mockAppConfig, times(1)).showAlphaBanner
      verify(mockAppConfig, times(1)).showBetaBanner
      verify(mockAppConfig, times(1)).showHelpImproveBanner
      verify(mockAppConfig, times(1)).serviceNameKey
      verify(mockAppConfig, times(1)).keepAliveUrl
      verify(mockAppConfig, times(1)).disableSessionExpired
      verify(mockWebchatUtil, times(1)).getWebchatScripts(any())

      menuCaptor.getValue mustBe menu
      serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
      serviceNameUrlCaptor.getValue mustBe None
      pageTitleCaptor.getValue mustBe None
      sideBarContentCaptor.getValue mustBe None
      signoutUrlCaptor.getValue mustBe Some("Signout-Url")
      keepAliveUrlCaptor.getValue mustBe "/refresh-session"
      showBackLinkJSCaptor.getValue mustBe false
      backLinkUrlCaptor.getValue mustBe None
      showSignOutInHeaderCaptor.getValue mustBe false
      scriptsCaptor.getValue mustBe Seq.empty
      styleSheetsCaptor.getValue mustBe Seq.empty
      bannerConfigCaptor.getValue mustBe BannerConfig(
        showAlphaBanner = true,
        showBetaBanner = false,
        showHelpImproveBanner = true
      )
      optTrustedHelperCaptor.getValue mustBe None
      fullWidthCaptor.getValue mustBe true
      disableSessionExpiredCaptor.getValue mustBe false
      contentCaptor.getValue mustBe Html("Default-Content")
    }

    "return default New Sca layout with UR banner link from new data response" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", defaultUrBanner.page)
        .withAttrs(
          TypedMap(
            Keys.wrapperDataKey    -> wrapperDataResponse,
            Keys.messageDataKey    -> 2,
            RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
          )
        )
      when(mockAppConfig.showAlphaBanner).thenReturn(true)
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)
      when(mockWebchatUtil.getWebchatScripts(any())).thenReturn(Seq.empty[Html])

      wrapperService.standardScaLayout(content = Html("Default-Content"), serviceURLs = serviceUrls)(messages, request)

      verify(mockStandardScaLayout, times(1)).apply(
        menu = menuCaptor.capture(),
        serviceURLs = serviceURLsCaptor.capture(),
        serviceNameKey = serviceNameKeyCaptor.capture(),
        pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(),
        keepAliveUrl = keepAliveUrlCaptor.capture(),
        showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(),
        showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(),
        styleSheets = styleSheetsCaptor.capture(),
        bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture(),
        urBannerUrl = urBannerUrlCaptor.capture()
      )(contentCaptor.capture())(any(), any())

      verify(mockAppConfig, times(1)).showAlphaBanner
      verify(mockAppConfig, times(1)).showBetaBanner
      verify(mockAppConfig, times(1)).showHelpImproveBanner
      verify(mockAppConfig, times(1)).serviceNameKey
      verify(mockAppConfig, times(1)).keepAliveUrl
      verify(mockAppConfig, times(1)).disableSessionExpired
      verify(mockAppConfig, times(0)).helpImproveBannerUrl
      verify(mockWebchatUtil, times(1)).getWebchatScripts(any())

      menuCaptor.getValue mustBe standardmenu
      serviceURLsCaptor.getValue mustBe serviceUrls
      serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
      pageTitleCaptor.getValue mustBe None
      sideBarContentCaptor.getValue mustBe None
      keepAliveUrlCaptor.getValue mustBe "/refresh-session"
      showBackLinkJSCaptor.getValue mustBe false
      backLinkUrlCaptor.getValue mustBe None
      showSignOutInHeaderCaptor.getValue mustBe false
      scriptsCaptor.getValue mustBe Seq.empty
      styleSheetsCaptor.getValue mustBe Seq.empty
      bannerConfigCaptor.getValue mustBe BannerConfig(
        showAlphaBanner = true,
        showBetaBanner = false,
        showHelpImproveBanner = true
      )
      optTrustedHelperCaptor.getValue mustBe None
      fullWidthCaptor.getValue mustBe true
      disableSessionExpiredCaptor.getValue mustBe false
      urBannerUrlCaptor.getValue mustBe Some(defaultUrBanner.link)
      contentCaptor.getValue mustBe Html("Default-Content")
    }

    "return default New Sca layout with old UR banner, help improve link" in {

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "NON MATCHING URI")
        .withAttrs(
          TypedMap(
            Keys.wrapperDataKey    -> wrapperDataResponse,
            Keys.messageDataKey    -> 2,
            RequestAttrKey.Cookies -> Cell(Cookies(Seq(Cookie("PLAY_LANG", "en"))))
          )
        )

      when(mockAppConfig.helpImproveBannerUrl).thenReturn(Some("config link"))
      when(mockAppConfig.showBetaBanner).thenReturn(false)
      when(mockAppConfig.showHelpImproveBanner).thenReturn(true)
      when(mockAppConfig.serviceNameKey).thenReturn(Some("Default-Service-Name-Key"))
      when(mockAppConfig.keepAliveUrl).thenReturn("/refresh-session")
      when(mockAppConfig.disableSessionExpired).thenReturn(false)
      when(mockWebchatUtil.getWebchatScripts(any())).thenReturn(Seq.empty[Html])

      wrapperService.standardScaLayout(
        content = Html("Default-Content"),
        serviceURLs = serviceUrls,
        bannerConfig = BannerConfig(false, true, true)
      )(messages, request)

      verify(mockStandardScaLayout, times(1)).apply(
        menu = menuCaptor.capture(),
        serviceURLs = serviceURLsCaptor.capture(),
        serviceNameKey = serviceNameKeyCaptor.capture(),
        pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(),
        keepAliveUrl = keepAliveUrlCaptor.capture(),
        showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(),
        showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(),
        styleSheets = styleSheetsCaptor.capture(),
        bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture(),
        urBannerUrl = urBannerUrlCaptor.capture()
      )(contentCaptor.capture())(any(), any())

      verify(mockAppConfig, times(1)).helpImproveBannerUrl
      verify(mockWebchatUtil, times(1)).getWebchatScripts(any())

      menuCaptor.getValue mustBe standardmenu
      serviceURLsCaptor.getValue mustBe serviceUrls
      serviceNameKeyCaptor.getValue mustBe Some("Default-Service-Name-Key")
      pageTitleCaptor.getValue mustBe None
      sideBarContentCaptor.getValue mustBe None
      keepAliveUrlCaptor.getValue mustBe "/refresh-session"
      showBackLinkJSCaptor.getValue mustBe false
      backLinkUrlCaptor.getValue mustBe None
      showSignOutInHeaderCaptor.getValue mustBe false
      scriptsCaptor.getValue mustBe Seq.empty
      styleSheetsCaptor.getValue mustBe Seq.empty
      bannerConfigCaptor.getValue mustBe BannerConfig(
        showAlphaBanner = false,
        showBetaBanner = true,
        showHelpImproveBanner = true
      )
      optTrustedHelperCaptor.getValue mustBe None
      fullWidthCaptor.getValue mustBe true
      disableSessionExpiredCaptor.getValue mustBe false
      urBannerUrlCaptor.getValue mustBe Some("config link")
      contentCaptor.getValue mustBe Html("Default-Content")
    }

    "return layout" in {

      val content               = Html("Content")
      val pageTitle             = Some("Page-Title")
      val serviceNameKey        = Some("Service-Name-Key")
      val serviceNameUrl        = Some("Service-Name-Url")
      val sidebarContent        = Some(Html("Sidebar-Content"))
      val signoutUrl            = Some("Signout-Url")
      val timeOutUrl            = Some("Timeout-Url")
      val keepAliveUrl          = "Keep-Alive-Url"
      val showBackLinkJS        = true
      val backLinkUrl           = Some("Backlink-Url")
      val showSignOutInHeader   = true
      val scripts               = Seq(Html("Scripts"))
      val styleSheets           = Seq(Html("StyleSheets"))
      val bannerConfig          = BannerConfig(showAlphaBanner = false, showBetaBanner = true, showHelpImproveBanner = false)
      val optTrustedHelper      =
        Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", Some("principalNino")))
      val fullWidth             = true
      val hideMenuBar           = true
      val disableSessionExpired = true

      when(mockWebchatUtil.getWebchatScripts(any())).thenReturn(Seq.empty[Html])

      wrapperService.layout(
        content,
        pageTitle,
        serviceNameKey,
        serviceNameUrl,
        sidebarContent,
        signoutUrl,
        timeOutUrl,
        keepAliveUrl,
        showBackLinkJS,
        backLinkUrl,
        scripts,
        styleSheets,
        bannerConfig,
        optTrustedHelper,
        fullWidth,
        hideMenuBar,
        disableSessionExpired
      )

      verify(mockScaLayout, times(1)).apply(
        menu = menuCaptor.capture(),
        serviceNameKey = serviceNameKeyCaptor.capture(),
        serviceNameUrl = serviceNameUrlCaptor.capture(),
        pageTitle = pageTitleCaptor.capture(),
        sidebarContent = sideBarContentCaptor.capture(),
        signoutUrl = signoutUrlCaptor.capture(),
        timeOutUrl = timeOutUrlCaptor.capture(),
        keepAliveUrl = keepAliveUrlCaptor.capture(),
        showBackLinkJS = showBackLinkJSCaptor.capture(),
        backLinkUrl = backLinkUrlCaptor.capture(),
        showSignOutInHeader = showSignOutInHeaderCaptor.capture(),
        scripts = scriptsCaptor.capture(),
        styleSheets = styleSheetsCaptor.capture(),
        bannerConfig = bannerConfigCaptor.capture(),
        fullWidth = fullWidthCaptor.capture(),
        disableSessionExpired = disableSessionExpiredCaptor.capture(),
        optTrustedHelper = optTrustedHelperCaptor.capture(),
        any()
      )(contentCaptor.capture())(any(), any())

      verify(mockAppConfig, never).showAlphaBanner
      verify(mockAppConfig, never).showBetaBanner
      verify(mockAppConfig, never).showHelpImproveBanner
      verify(mockAppConfig, never).serviceNameKey
      verify(mockAppConfig, never).keepAliveUrl
      verify(mockAppConfig, never).disableSessionExpired
      verify(mockWebchatUtil, times(1)).getWebchatScripts(any())

      menuCaptor.getValue mustBe None
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
      disableSessionExpiredCaptor.getValue mustBe disableSessionExpired
      contentCaptor.getValue mustBe content
    }

    "return the continueUrl if it is a valid relative URL" in {
      val continueUrl = Some(RedirectUrl("/url"))

      val actualUrl = wrapperService.safeSignoutUrl(continueUrl)

      actualUrl mustBe Some("/url")
    }

    "return the exitSurveyOrigin if the continueUrl is None" in {
      val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

      when(mockAppConfig.exitSurveyOrigin).thenReturn(Some("origin"))
      when(mockAppConfig.enc).thenReturn(URLEncoder.encode(_: String, "UTF-8"))

      val expectedUrl =
        appConfig.exitSurveyOrigin.map(origin => appConfig.feedbackFrontendUrl + "/" + appConfig.enc(origin))

      val response =
        new WrapperService(mockPtaMenuBar, mockScaLayout, mockStandardScaLayout, mockWebchatUtil, appConfig)
          .safeSignoutUrl(None)

      response mustBe expectedUrl
    }
  }
}

object WrapperServiceSpec {

  val ptaMenuConfig: PtaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")
  val menuItemConfig1: MenuItemConfig = MenuItemConfig(
    "home",
    "Account home",
    "pertaxUrl",
    leftAligned = true,
    position = 0,
    Some("hmrc-account-icon hmrc-account-icon--home"),
    None
  )
  val menuItemConfig2: MenuItemConfig =
    MenuItemConfig("messages", "Messages", "pertaxUrl-messages", leftAligned = false, position = 0, None, None)
  val menuItemConfig3: MenuItemConfig =
    MenuItemConfig("progress", "Check progress", "trackingUrl-track", leftAligned = false, position = 1, None, None)
  val menuItemConfig4: MenuItemConfig = MenuItemConfig(
    "profile",
    "Profile and settings",
    "pertaxUrl-profile-and-settings",
    leftAligned = false,
    position = 2,
    None,
    None
  )
  val menuItemConfig5: MenuItemConfig = MenuItemConfig(
    "signout",
    "Sign out",
    "pertaxUrl-signout-feedback-PERTAX",
    leftAligned = false,
    position = 3,
    None,
    None
  )
  val menu: Option[Html]              = Some(
    Html(
      "\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\"\n   class=\"hmrc-account-menu__link hmrc-account-menu__link--home\n   \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Account menu\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n    <span class=\"hmrc-notification-badge\">2</span>\n\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"Signout-Url\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n"
    )
  )
  val standardmenu: Option[Html]      = Some(
    Html(
      "\n<!-- ACCOUNT MENU -->\n<nav id=\"secondary-nav\" class=\"hmrc-account-menu\" aria-label=\"Account\" data-module=\"hmrc-account-menu\">\n<!-- LEFT ALIGNED ITEMS -->\n            \n                \n<a href=\"pertaxUrl\"\n   class=\"hmrc-account-menu__link hmrc-account-menu__link--home\n   \" id=\"menu.left.0\">\n \n <span class=\"hmrc-account-icon hmrc-account-icon--home\">\n Account home\n </span>\n \n</a>\n\n            \n<!-- LEFT ALIGNED ITEMS -->\n    <a id=\"menu.name\" href=\"#\" class=\"hmrc-account-menu__link hmrc-account-menu__link--menu js-hidden js-visible\" tabindex=\"-1\" aria-hidden=\"true\" aria-expanded=\"false\">\n        Account menu\n    </a>\n    <ul class=\"hmrc-account-menu__main\">\n        <li class=\"hmrc-account-menu__link--back hidden\" aria-hidden=\"false\">\n            <a id=\"menu.back\" href=\"#\" tabindex=\"-1\" class=\"hmrc-account-menu__link\">\n            Back\n            </a>\n        </li>\n<!-- RIGHT ALIGNED ITEMS -->\n        \n                \n<li>\n <a href=\"pertaxUrl-messages\" class=\"hmrc-account-menu__link \" id=\"menu.right.0\">\n \n  <span class=\"\">\n   Messages\n   \n    <span class=\"hmrc-notification-badge\">2</span>\n\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"trackingUrl-track\" class=\"hmrc-account-menu__link \" id=\"menu.right.1\">\n \n  <span class=\"\">\n   Check progress\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"pertaxUrl-profile-and-settings\" class=\"hmrc-account-menu__link \" id=\"menu.right.2\">\n \n  <span class=\"\">\n   Profile and settings\n   \n  </span>\n \n </a>\n</li>\n\n            \n                \n<li>\n <a href=\"Signout-Url\" class=\"hmrc-account-menu__link \" id=\"menu.right.3\">\n \n  <span class=\"\">\n   Sign out\n   \n  </span>\n \n </a>\n</li>\n\n            \n<!-- RIGHT ALIGNED ITEMS -->\n    </ul>\n</nav>\n"
    )
  )

  val defaultUrBanner: UrBanner = UrBanner("test-page", "test-link", isEnabled = true)
  val defaultWebchat: Webchat   = Webchat("test-page", "popup", isEnabled = true)

  private val wrapperDataResponse: WrapperDataResponse = WrapperDataResponse(
    Seq(menuItemConfig1, menuItemConfig2, menuItemConfig3, menuItemConfig4, menuItemConfig5),
    ptaMenuConfig,
    List(defaultUrBanner),
    List(defaultWebchat)
  )

  val menuCaptor: ArgumentCaptor[Option[Html]] = ArgumentCaptor.forClass(classOf[Option[Html]])
  val serviceURLsCaptor                        = ArgumentCaptor.forClass(classOf[ServiceURLs])
  val serviceNameKeyCaptor                     = ArgumentCaptor.forClass(classOf[Option[String]])
  val serviceNameUrlCaptor                     = ArgumentCaptor.forClass(classOf[Option[String]])
  val pageTitleCaptor                          = ArgumentCaptor.forClass(classOf[Option[String]])
  val sideBarContentCaptor                     = ArgumentCaptor.forClass(classOf[Option[Html]])
  val signoutUrlCaptor                         = ArgumentCaptor.forClass(classOf[Option[String]])
  val timeOutUrlCaptor                         = ArgumentCaptor.forClass(classOf[Option[String]])
  val keepAliveUrlCaptor                       = ArgumentCaptor.forClass(classOf[String])
  val showBackLinkJSCaptor                     = ArgumentCaptor.forClass(classOf[Boolean])
  val backLinkUrlCaptor                        = ArgumentCaptor.forClass(classOf[Option[String]])
  val showSignOutInHeaderCaptor                = ArgumentCaptor.forClass(classOf[Boolean])
  val scriptsCaptor                            = ArgumentCaptor.forClass(classOf[Seq[HtmlFormat.Appendable]])
  val styleSheetsCaptor                        = ArgumentCaptor.forClass(classOf[Seq[HtmlFormat.Appendable]])
  val bannerConfigCaptor                       = ArgumentCaptor.forClass(classOf[BannerConfig])
  val optTrustedHelperCaptor                   = ArgumentCaptor.forClass(classOf[Option[TrustedHelper]])
  val fullWidthCaptor                          = ArgumentCaptor.forClass(classOf[Boolean])
  val disableSessionExpiredCaptor              = ArgumentCaptor.forClass(classOf[Boolean])
  val contentCaptor                            = ArgumentCaptor.forClass(classOf[Html])
  val urBannerUrlCaptor                        = ArgumentCaptor.forClass(classOf[Option[String]])
}
