package org.ababup1192

import java.net.URI

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.{
  DeleteMessageRequest,
  ReceiveMessageRequest
}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import scala.util.Random

object Main extends App {
  while (true) {

    val client = SqsAsyncClient
      .builder()
      .region(Region.AP_NORTHEAST_1)
      .endpointOverride(new URI("http://localhost:4576"))
      .build()

    val receiveMessageRequest = ReceiveMessageRequest
      .builder()
      .queueUrl("http://localhost:4567/queue/queue")
      .maxNumberOfMessages(10)
      .build()

    println("receive message start")
    (for {
      response <- Future(client.receiveMessage(receiveMessageRequest).get())
      messageBodyList <- Future.sequence(response.messages().asScala.map {
        message =>
          Future {
            Thread.sleep(Random.nextInt(10))
            val deleteMessageRequest = DeleteMessageRequest
              .builder()
              .queueUrl("http://localhost:4567/queue/queue")
              .receiptHandle(message.receiptHandle())
              .build()
            client.deleteMessage(deleteMessageRequest).get()

            message.body()
          }
      })
    } yield messageBodyList).foreach { messageBodyList =>
      println(messageBodyList)
    }

    Thread.sleep(5 * 1000)
  }
}
