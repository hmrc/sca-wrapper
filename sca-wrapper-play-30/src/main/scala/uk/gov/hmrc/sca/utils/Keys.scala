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

package uk.gov.hmrc.sca.utils

import play.api.libs.typedmap.TypedKey
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.sca.models.WrapperDataResponse

object Keys {
  lazy val wrapperIsAuthenticatedKey: TypedKey[Boolean]  = TypedKey[Boolean]
  lazy val wrapperFilterHasRun: TypedKey[Boolean]        = TypedKey[Boolean]
  lazy val wrapperDataKey: TypedKey[WrapperDataResponse] = TypedKey[WrapperDataResponse]
  lazy val messageDataKey: TypedKey[Int]                 = TypedKey[Int]
  lazy val trustedHelperKey: TypedKey[TrustedHelper]     = TypedKey[TrustedHelper]
}
