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

package controllers

import play.api.test.Helpers
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.sca.controllers.KeepAliveController
import utils.BaseSpec
import org.scalatest.matchers.should.Matchers._

class KeepAliveControllerSpec extends BaseSpec {

  override implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))

  private val controller = new KeepAliveController(Helpers.stubMessagesControllerComponents())
  val nino               = "AA999999A"

  "KeepAliveController keepAliveUnauthenticated" must {
    "return OK given an unauthenticated request" in {

      val result = controller.keepAlive()(fakeRequest)
      whenReady(result) { res =>
        res.header.status shouldBe 200
      }
    }
  }
}
