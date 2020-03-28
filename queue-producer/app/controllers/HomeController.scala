package controllers

import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject._
import play.api.libs.circe.Circe
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class User(id: Int, name: String)
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BaseController
    with Circe {

  def index() = Action.async { implicit request: Request[AnyContent] =>
    Future(Ok(User(1, "John").asJson))
  }
}
