package controllers

import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject._
import play.api.libs.circe.Circe
import play.api.mvc._
import play.mvc.Http.MimeTypes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

import scala.concurrent.{ExecutionContext, Future}

case class SendMessageResponse(messageId: String,
                               content: String,
                               sentAt: String)
@Singleton
class ProducerController @Inject()(
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController
    with Circe {

  def sendMessage(): Action[Json] = Action(circe.json(1024)).async {
    implicit request: Request[Json] =>
      val content =
        request.body.hcursor.downField("content").as[String].getOrElse("")
      val client = SqsAsyncClient
        .builder()
        .region(Region.AP_NORTHEAST_1)
        .endpointOverride(new URI("http://localhost:4576"))
        .build()

      val now = LocalDateTime.now()
      val sentAt =
        now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss"))

      val sendMessageRequest = SendMessageRequest
        .builder()
        .queueUrl("http://localhost:4576/queue/queue")
        .messageBody(content)
        .build()

      Future(client.sendMessage(sendMessageRequest).get())
        .map(sendMessageResponse => {

          Ok(
            SendMessageResponse(
              messageId = sendMessageResponse.messageId(),
              content = content,
              sentAt = sentAt
            ).asJson
          ).withHeaders(CONTENT_TYPE -> MimeTypes.JSON)
        })
  }

  def commonOption(): Action[AnyContent] =
    Action.async(Future.successful(NoContent))
}
