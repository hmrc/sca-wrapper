/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.sca.utils

import play.api.Logging
import play.api.inject.Injector
import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.sca.config.AppConfig
import uk.gov.hmrc.sca.models.{Webchat, WrapperDataResponse}
import uk.gov.hmrc.webchat.client.WebChatClient

import javax.inject.Inject

class WebchatUtil @Inject() (appConfig: AppConfig, injector: Injector) extends Logging {

  private def getChatType(webchat: Webchat, webChatClient: WebChatClient)(implicit requestHeader: RequestHeader) =
    if (webchat.chatType.equals("loadWebChatContainer"))
      webChatClient.loadWebChatContainer(webchat.skinElement)(requestHeader.withBody(""))
    else webChatClient.loadHMRCChatSkinElement(webchat.skinElement)(requestHeader.withBody(""))

  def getWebchatScripts(implicit requestHeader: RequestHeader): Seq[HtmlFormat.Appendable] = {
    val wrapperDataResponse: Option[WrapperDataResponse] = requestHeader.attrs.get(Keys.wrapperDataKey)
    wrapperDataResponse.fold(Seq.empty[Html]) { response =>
      response.webchatPages.find(webchat => requestHeader.path.matches(webchat.pattern)).fold(Seq.empty[Html]) {
        webchatConfig =>
          if (webchatConfig.isEnabled) {
            if (appConfig.webChatHashingKey.isEmpty || appConfig.webChatKey.isEmpty) {
              val msg =
                "Webchat enabled but there is one or more missing webchat key(s). Keys " +
                  "request-body-encryption.hashing-key and request-body-encryption.key must both be present " +
                  "in application.conf."
              logger.error(msg, new RuntimeException(msg))
              Seq.empty[Html]
            } else {
              val webChatClient: WebChatClient = injector.instanceOf[WebChatClient]
              Seq(
                webChatClient.loadRequiredElements()(requestHeader.withBody("")),
                getChatType(webchatConfig, webChatClient)
              ).flatten
            }
          } else Seq.empty[Html]
      }
    }
  }
}
