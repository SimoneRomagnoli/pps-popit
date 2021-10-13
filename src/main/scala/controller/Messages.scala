package controller

object Messages {
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message

  trait SpawnerMessage extends Update

  sealed trait MessageType
  case object Spawn extends MessageType

  val messageType: Update => MessageType = {
    case _: SpawnerMessage => Spawn
    case _                 => Spawn
  }

}
