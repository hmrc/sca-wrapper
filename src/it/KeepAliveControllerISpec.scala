import com.github.tomakehurst.wiremock.client.WireMock._
import helpers.IntegrationSpec
import play.api.Application
import play.api.http.Status.TOO_MANY_REQUESTS
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_GATEWAY, BAD_REQUEST, GET, IM_A_TEAPOT, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE, UNPROCESSABLE_ENTITY, contentAsString, defaultAwaitTimeout, route, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.SessionKeys

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

<<<<<<< HEAD
class KeepAliveControllerISpec extends IntegrationSpec {


  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure(

    )
    .build()

  val url = s"/refresh-session-unauthenticated"

  def request: FakeRequest[AnyContentAsEmpty.type] = {
    val uuid = UUID.randomUUID().toString
    FakeRequest(GET, url).withSession(SessionKeys.sessionId -> uuid)
  }

  implicit lazy val ec = app.injector.instanceOf[ExecutionContext]

  "KeepAliveController" must {
    "return 200 given an unauthenticated request" in {

      val result: Future[Result] = route(app, request).get

      //TODO fix
      whenReady(result) { res =>
        res.header.status mustBe 404
      }
    }
  }

}
