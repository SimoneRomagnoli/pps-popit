package controller

import akka.actor.typed.ActorRef
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.maps.Cells.Cell
import model.maps.Tracks.Track
import model.stats.Stats.GameStats

object Messages {
  //TRAITS
  trait Message
  trait Render extends Message
  trait Input extends Message
  trait Update extends Message

  //VIEW
  case class RenderStats(stats: GameStats) extends Render
  case class RenderEntities(entities: List[Entity]) extends Render
  case class RenderMap(track: Track) extends Render

  //CONTROLLER
  case class NewGame() extends Input
  case class PauseGame() extends Input
  case class ResumeGame() extends Input
  case class NewTimeRatio(value: Double) extends Input
  case class PlaceTower[B <: Bullet](cell: Cell, towerType: TowerType[B]) extends Input
  case class CurrentWallet(amount: Int) extends Input
  case class TowerOption(tower: Option[Tower[_]]) extends Input with Update

  sealed trait Interaction extends Input {
    def replyTo: ActorRef[Message]
    def request: Message
  }

  case class MvcInteraction(override val replyTo: ActorRef[Message], override val request: Message)
      extends Interaction

  //GAME LOOP
  case object Tick extends Input
  case class Start() extends Input
  case class MapCreated(track: Track) extends Input
  case class ModelUpdated(entities: List[Entity], stats: GameStats) extends Input

  //MODEL
  case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input]) extends Update
  case class NewMap(replyTo: ActorRef[Input]) extends Update
  case class WalletQuantity(replyTo: ActorRef[Input]) extends Update
  case class Pay(amount: Int) extends Update

  case class UpdateEntity(
      elapsedTime: Double,
      entities: List[Entity],
      replyTo: ActorRef[Update],
      track: Track)
      extends Update
  case class EntityUpdated(entity: Entity) extends Update
  case class SpawnEntity(entity: Entity) extends Update
  case class EntitySpawned(entity: Entity, actor: ActorRef[Update]) extends Update
  case class EntityKilled(entity: Entity, actorRef: ActorRef[Update]) extends Update
  case class ExitedBalloon(balloon: Balloon, actorRef: ActorRef[Update]) extends Update
  case class TowerIn(cell: Cell) extends Update

  //TOWER
  case class SearchBalloon(replyTo: ActorRef[Update], balloon: Balloon) extends Update
  case class BalloonDetected() extends Update
  case class UpdatePosition(replyTo: ActorRef[Update]) extends Update
  case class Tick(replyTo: ActorRef[Update]) extends Update
  case class BalloonMoved(balloon: Balloon) extends Update
}
