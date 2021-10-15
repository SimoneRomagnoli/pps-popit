package controller

import akka.actor.typed.ActorRef

object Messages {
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message

  trait SpawnManagerMessage extends Update
  trait EntitiesManagerMessage extends Update
  trait GameDynamicsManagerMessage extends Update

  sealed trait MessageType
  case object SpawnMessage extends MessageType
  case object EntityMessage extends MessageType
  case object GameDynamicsMessage extends MessageType

  val messageType: Update => MessageType = {
    case _: SpawnManagerMessage        => SpawnMessage
    case _: EntitiesManagerMessage     => EntityMessage
    case _: GameDynamicsManagerMessage => GameDynamicsMessage
    case _                             => SpawnMessage
  }

  case class WithReplyTo[T <: Update](message: T, replyTo: ActorRef[Input])
      extends Update
      with Input

}
