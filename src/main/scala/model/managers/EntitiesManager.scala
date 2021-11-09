package model.managers

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.util.Timeout
import controller.Controller.ControllerMessages.{ CurrentWallet, StartNextRound }
import controller.interaction.GameLoop.GameLoopMessages.{ CanStartNextRound, ModelUpdated }
import controller.interaction.Messages.{ EntitiesManagerMessage, Input, Update, WithReplyTo }
import model.Model.ModelMessages.{ TickUpdate, TrackChanged }
import model.actors.BalloonMessages.{ BalloonKilled, Hit }
import model.actors.BulletMessages.{ BalloonHit, BulletKilled, StartExplosion }
import model.actors.TowerMessages.Boost
import model.actors.{ BalloonActor, BulletActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.PowerUps.{ BoostedTower, TowerPowerUp }
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages._
import model.managers.GameDataMessages.{ Lose, Pay, WalletQuantity }
import model.maps.Cells.Cell
import model.maps.Tracks.Track
import commons.Futures.retrieve
import model.entities.towers.TowerValues.maxLevel

import scala.concurrent.duration.DurationInt

case class EntityActor(actorRef: ActorRef[Update], entity: Entity)

object EntitiesMessages {

  case class UpdateEntity(elapsedTime: Double, entities: List[Entity], replyTo: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage

  case class EntityUpdated(entity: Entity, ref: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage
  case class SpawnEntity(entity: Entity) extends Update with EntitiesManagerMessage

  case class EntitySpawned(entity: Entity, actor: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage

  case class ExitedBalloon(balloon: Balloon, actorRef: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage

  case class PlaceTower[B <: Bullet](cell: Cell, towerType: TowerType[B])
      extends Input
      with Update
      with EntitiesManagerMessage

  case class TowerIn(cell: Cell) extends Update with EntitiesManagerMessage
  case class Selectable(cell: Cell) extends Update with EntitiesManagerMessage
  case class DoneSpawning() extends Update with EntitiesManagerMessage

  case class BoostTowerIn(cell: Cell, powerUp: TowerPowerUp)
      extends Update
      with EntitiesManagerMessage
  case class TowerOption(tower: Option[Tower[Bullet]]) extends Input with Update
  case class Selected(selectable: Boolean) extends Input with Update
}

object EntitiesManager {

  def apply(model: ActorRef[Update]): Behavior[Update] = Behaviors.setup { ctx =>
    EntityManager(ctx, model).running()
  }
}

case class EntityManager private (
    ctx: ActorContext[Update],
    model: ActorRef[Update],
    var spawning: Boolean = false,
    var track: Track = Track(),
    var entities: List[EntityActor] = List(),
    var messageQueue: Seq[Update] = Seq()) {
  implicit val timeout: Timeout = 3.seconds
  implicit val scheduler: Scheduler = ctx.system.scheduler

  def dequeueAndRun(): Behavior[Update] = {
    messageQueue.foreach(ctx.self ! _)
    messageQueue = Seq()
    running()
  }

  def dequeueAndUpdate(replyTo: ActorRef[Input]): Behavior[Update] = {
    messageQueue.foreach(ctx.self ! _)
    messageQueue = Seq()
    updating(replyTo)
  }

  def running(): Behavior[Update] = Behaviors.receiveMessage {
    case TrackChanged(newTrack) =>
      track = newTrack
      Behaviors.same

    case TickUpdate(elapsedTime, replyTo) =>
      checkRoundOver(replyTo)
      entities.map(_.actorRef).foreach {
        _ ! UpdateEntity(elapsedTime, entities.map(_.entity), ctx.self)
      }
      dequeueAndUpdate(replyTo)

    case SpawnEntity(entity) =>
      ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
      Behaviors.same

    case EntitySpawned(entity, actor) =>
      entities = EntityActor(actor, entity) :: entities
      Behaviors.same

    case StartNextRound() =>
      spawning = true
      Behaviors.same

    case DoneSpawning() =>
      spawning = false
      Behaviors.same

    case WithReplyTo(msg, replyTo) =>
      msg match {
        case Selectable(cell) =>
          val selectable: Boolean = track.cells.forall(c => c.x != cell.x || c.y != cell.y) &&
            entities
              .map(_.entity)
              .collect { case e: Tower[Bullet] => e }
              .forall(t => !cell.contains(t.position))
          replyTo ! Selected(selectable)

        case PlaceTower(cell, towerType) =>
          retrieve(model ? WalletQuantity) {
            case CurrentWallet(amount) =>
              if (amount >= towerType.cost) {
                val tower: Tower[Bullet] = towerType.spawn in cell
                model ! SpawnEntity(tower)
                model ! Pay(towerType.cost)
              }
            case _ =>
          }

        case TowerIn(cell) =>
          val tower: Option[Tower[Bullet]] = entities
            .map(_.entity)
            .collectFirst {
              case tower: Tower[Bullet] if tower.position == cell.centralPosition => tower
            }
          replyTo ! TowerOption(tower)

        case BoostTowerIn(cell, powerUp) =>
          val (towerActor, towerInstance) = entities.collect {
            case EntityActor(actorRef, entity)
                if entity.isInstanceOf[Tower[_]] && cell.contains(entity.position) =>
              (actorRef, entity.asInstanceOf[Tower[Bullet]])
          }.head
          if ((towerInstance levelOf powerUp) != maxLevel) {
            retrieve(model ? WalletQuantity) {
              case CurrentWallet(amount) if amount - powerUp.cost > 0 =>
                model ! Pay(powerUp.cost)
                towerActor ! Boost(powerUp, replyTo)
              case _ =>
            }
          }

      }
      Behaviors.same

    case msg => enqueue(msg)
  }

  def updating(
      replyTo: ActorRef[Input],
      updatedEntities: List[EntityActor] = List(),
      animations: List[Entity] = List()): Behavior[Update] = Behaviors.receiveMessage {
    case EntityUpdated(entity, ref) =>
      EntityActor(ref, entity) :: updatedEntities match {
        case full if full.size == entities.size =>
          val (balloons, others): (List[Entity], List[Entity]) =
            full.map(_.entity).partition(_.isInstanceOf[Balloon])

          replyTo ! ModelUpdated(
            others.appendedAll(balloons.asInstanceOf[List[Balloon]].sorted),
            animations
          )
          entities = full
          dequeueAndRun()
        case notFull => updating(replyTo, notFull, animations)
      }

    case ExitedBalloon(balloon, actorRef) =>
      model ! Lose(balloon.life)
      ctx.self ! BalloonKilled(actorRef)
      Behaviors.same

    case BulletKilled(actorRef) =>
      killEntity(updatedEntities, replyTo, actorRef, animations)

    case BalloonKilled(actorRef) =>
      if (updatedEntities.map(_.actorRef).contains(actorRef)) {
        entities = entities.filter(_.actorRef != actorRef)
        updating(replyTo, updatedEntities.filter(_.actorRef != actorRef), animations)
      } else {
        killEntity(updatedEntities, replyTo, actorRef, animations)
      }

    case BalloonHit(bullet, balloons) =>
      entities.filter(e => balloons.contains(e.entity)).foreach { balloon =>
        balloon.actorRef ! Hit(bullet, model)
      }
      Behaviors.same

    case StartExplosion(bullet) =>
      updating(replyTo, updatedEntities, bullet :: animations)

    case EntitySpawned(entity, actor) =>
      entities = EntityActor(actor, entity) :: entities
      ctx.self ! EntityUpdated(entity, actor)
      updating(replyTo, updatedEntities, animations)

    case TickUpdate(_, _) if entities.isEmpty =>
      dequeueAndRun()

    case msg => enqueue(msg)

  }

  def killEntity(
      updatedEntities: List[EntityActor],
      replyTo: ActorRef[Input],
      actorRef: ActorRef[Update],
      animations: List[Entity]): Behavior[Update] = updatedEntities match {
    case full if full.size == entities.size - 1 =>
      replyTo ! ModelUpdated(full.map(_.entity), animations)
      entities = full
      dequeueAndRun()
    case notFull =>
      entities = entities.filter(_.actorRef != actorRef)
      updating(replyTo, notFull, animations)
  }

  def entitySpawned(entity: Entity, ctx: ActorContext[Update]): ActorRef[Update] = entity match {
    case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
    case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
    case bullet: Bullet   => ctx.spawnAnonymous(BulletActor(bullet))
  }

  def checkRoundOver(replyTo: ActorRef[Input]): Unit =
    if (entities.collect { case EntityActor(_, b: Balloon) =>
        b
      }.isEmpty && !spawning)
      replyTo ! CanStartNextRound()

  def enqueue(msg: Update): Behavior[Update] = {
    if (!msg.isInstanceOf[TickUpdate]) {
      messageQueue = messageQueue :+ msg
    }
    Behaviors.same
  }
}
