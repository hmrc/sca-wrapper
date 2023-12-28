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

package fixtures

import play.api.mvc.Request
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.{ConfidenceLevel, Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.sca.models.auth.AuthenticatedRequest

object TestData {

  object Requests {
    def authenticatedRequest[A](request: Request[A]): AuthenticatedRequest[A] = AuthenticatedRequest(
      nino = Some(Nino("AA111111A")),
      credentials = Credentials(
        "providerId",
        "providerType"
      ),
      confidenceLevel = ConfidenceLevel.L250,
      name = Some(uk.gov.hmrc.auth.core.retrieve.Name(Some("John"), Some("Smith"))),
      trustedHelper = Some(TrustedHelper("principalName", "attorneyName", "returnLinkUrl", "principalNino")),
      profile = Some("profile"),
      enrolments = Set(Enrolment("key", Seq(EnrolmentIdentifier("key", "value")), "state", None)),
      request = request
    )
  }

}
