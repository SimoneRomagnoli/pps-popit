package controller

import akka.actor.typed.ActorRef
import model.tower.Position.Position

object Messages {
  //TRAITS
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message

  //VIEW
  case class RenderEntities(entities: List[Any]) extends Render

  //CONTROLLER
  case class NewGame() extends Input
  case class PauseGame() extends Input
  case class ResumeGame() extends Input
  case class NewTimeRatio(value: Double) extends Input

  //GAME LOOP
  case object Tick extends Input
  case class ModelUpdated(entities: List[Any]) extends Input

  //MODEL
  case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input]) extends Update
  case class UpdateEntity(elapsedTime: Double, entities: List[Any], replyTo: ActorRef[Update]) extends Update
  case class EntityUpdated(entity: Any) extends Update

  //TOWER
  case class SearchBalloon(replyTo: ActorRef[Update], position: Position, radius: Double) extends Update
  case class CollisionDetected() extends Update
  case class UpdatePosition(replyTo: ActorRef[Update]) extends Update
  case class Tick(replyTo: ActorRef[Update]) extends Update
  case class BalloonMoved(balloon: Any) extends Update
  case class Start() extends Input

}
