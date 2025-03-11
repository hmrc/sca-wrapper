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

package uk.gov.hmrc.sca.utils2or3

import play.api.mvc.RequestHeader
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.sca.models.WrapperDataResponse
import uk.gov.hmrc.sca.utils.Keys
import uk.gov.hmrc.webchat.client.WebChatClient

import javax.inject.Inject

class WebchatUtil @Inject() (webChatClient: WebChatClient) {
  def getWebchatScripts(implicit requestHeader: RequestHeader): Seq[HtmlFormat.Appendable] = {
    val wrapperDataResponse: Option[WrapperDataResponse] = requestHeader.attrs.get(Keys.wrapperDataKey)
    wrapperDataResponse.fold(Seq.empty[Html]) { response =>
      response.webchatPages.find(webchat => requestHeader.uri.matches(webchat.pattern)).fold(Seq.empty[Html]) {
        webchatConfig =>
          if (webchatConfig.isEnabled) {
            Seq(
              webChatClient.loadRequiredElements()(requestHeader.withBody("")),
              webChatClient.loadHMRCChatSkinElement(webchatConfig.skinElement)(requestHeader.withBody(""))
            ).flatten
          } else Seq.empty[Html]
      }
    }
  }
}
