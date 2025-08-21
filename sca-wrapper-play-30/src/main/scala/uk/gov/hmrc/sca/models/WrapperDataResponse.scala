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

package uk.gov.hmrc.sca.models

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper

case class WrapperDataResponse(
  menuItemConfig: Seq[MenuItemConfig],
  ptaMinMenuConfig: PtaMinMenuConfig,
  urBanners: List[UrBanner],
  webchatPages: List[Webchat],
  unreadMessageCount: Option[Int],
  trustedHelper: Option[TrustedHelper]
)
object WrapperDataResponse {
  implicit val format: OFormat[WrapperDataResponse] = {
    implicit val format: Format[TrustedHelper] = Json.format[TrustedHelper]
    Json.format[WrapperDataResponse]
  }
}
