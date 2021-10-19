package utils

import controller.Messages.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Futures {

  /** Handler for a generic future, supposedly received after an ask message. */
  def retrieve(future: Future[Message])(handler: PartialFunction[Message, Unit]): Unit =
    future onComplete {
      case Failure(_) =>
      case Success(value) =>
        value match {
          case msg => handler(msg)
          case _   =>
        }
    }
}
