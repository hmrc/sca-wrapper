/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}

case class UrBanner(
  page: String,
  link: String,
  isEnabled: Boolean,
  titleEn: Option[String] = None,
  titleCy: Option[String] = None,
  linkTextEn: Option[String] = None,
  linkTextCy: Option[String] = None,
  hideCloseButton: Option[Boolean] = None
) {
  lazy val isBespoke: Boolean =
    titleEn.isDefined ||
      titleCy.isDefined ||
      linkTextEn.isDefined ||
      linkTextCy.isDefined ||
      hideCloseButton.isDefined
}

object UrBanner {
  implicit val format: OFormat[UrBanner] = Json.format[UrBanner]
}
