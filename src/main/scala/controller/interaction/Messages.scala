package controller.interaction

import akka.actor.typed.ActorRef

import scala.annotation.tailrec

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

  def messageTypes: Update => List[MessageType] = { msg =>
    @tailrec
    def _messageTypes(msg: Update, types: List[MessageType] = List()): List[MessageType] =
      msg match {
        case _: SpawnManagerMessage if !types.contains(SpawnMessage) =>
          _messageTypes(msg, SpawnMessage :: types)
        case _: EntitiesManagerMessage if !types.contains(EntityMessage) =>
          _messageTypes(msg, EntityMessage :: types)
        case _: GameDynamicsManagerMessage if !types.contains(GameDynamicsMessage) =>
          _messageTypes(msg, GameDynamicsMessage :: types)
        case _ => types
      }
    _messageTypes(msg)
  }

  case class WithReplyTo[T <: Update](message: T, replyTo: ActorRef[Input])
      extends Update
      with Input

}
