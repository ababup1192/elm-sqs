package filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomSecurityHeaderFilter @Inject()(
  implicit
  val mat: Materializer
) extends Filter
    with HeaderNames {

  def apply(
    nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] =
    nextFilter(requestHeader).map { result =>
      result
        .withHeaders(
          ACCESS_CONTROL_ALLOW_ORIGIN -> "*",
          ACCESS_CONTROL_ALLOW_METHODS -> "GET,POST,OPTIONS",
          ACCESS_CONTROL_ALLOW_HEADERS -> "X-Requested-With, Origin, X-Csrftoken, Content-Type, Accept"
        )
    }

}
