/*
 * Copyright 2026 HM Revenue & Customs
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

package filters

import org.apache.pekko.actor.ActorSystem
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Langs
import play.api.mvc._
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import org.apache.pekko.stream.Materializer
import uk.gov.hmrc.sca.filters.LangQueryParamFilter
import scala.concurrent.ExecutionContext.Implicits.global

class LangQueryParamFilterSpec extends PlaySpec with GuiceOneAppPerSuite {

  implicit val system: ActorSystem = ActorSystem("lang-filter")
  implicit val mat: Materializer   = Materializer(system)

  private val langs: Langs = app.injector.instanceOf[Langs]
  private val filter       = new LangQueryParamFilter(langs)

  val next: RequestHeader => Future[Result] = _ => Future.successful(Ok("next"))

  "LangQueryParamFilter" should {

    "Add PLAY_LANG cookie for GET with a valid available lang" in {
      val req    = FakeRequest(GET, "/test?lang=en")
      val result = filter.apply(next)(req)

      status(result) mustBe OK

      val cookie = cookies(result).get("PLAY_LANG")
      cookie mustBe defined
      cookie.value.value mustBe "en"
    }

    "Do not add cookie for GET with invalid lang code" in {
      val req    = FakeRequest(GET, "/test?lang=dummy")
      val result = filter.apply(next)(req)

      status(result) mustBe OK
      cookies(result).get("PLAY_LANG") mustBe None
    }

    "Do not add cookie for non-GET even with a valid available lang" in {
      val req = FakeRequest(POST, "/test?lang=en")

      val result = filter.apply(next)(req)

      status(result) mustBe OK
      cookies(result).get("PLAY_LANG") mustBe None
    }
  }
}
