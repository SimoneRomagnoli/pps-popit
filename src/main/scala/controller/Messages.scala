package controller

import akka.actor.typed.ActorRef
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.maps.Grids.Grid
import model.maps.Tracks.Track

object Messages {
  //TRAITS
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message

  //VIEW
  case class RenderEntities(entities: List[Entity]) extends Render
  case class RenderMap(track: Track) extends Render
  case class RenderLoading() extends Render

  //CONTROLLER
  case class NewGame() extends Input
  case class PauseGame() extends Input
  case class ResumeGame() extends Input
  case class NewTimeRatio(value: Double) extends Input

  //GAME LOOP
  case object Tick extends Input
  case class Start() extends Input
  case class MapCreated(track: Track) extends Input
  case class ModelUpdated(entities: List[Entity]) extends Input

  //MODEL
  case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input]) extends Update
  case class NewMap(replyTo: ActorRef[Input]) extends Update

  case class UpdateEntity(
      elapsedTime: Double,
      entities: List[Entity],
      replyTo: ActorRef[Update],
      track: Track)
      extends Update
  case class EntityUpdated(entity: Entity) extends Update

  //TOWER
  case class SearchBalloon(replyTo: ActorRef[Update], balloon: Balloon) extends Update
  case class BalloonDetected() extends Update
  case class UpdatePosition(replyTo: ActorRef[Update]) extends Update
  case class Tick(replyTo: ActorRef[Update]) extends Update
  case class BalloonMoved(balloon: Balloon) extends Update
}
