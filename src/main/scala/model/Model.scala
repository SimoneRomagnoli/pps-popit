package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopMessages.{ MapCreated, ModelUpdated }
import controller.Messages._
import model.Model.ModelMessages._
import model.actors.BalloonMessages.{ BalloonKilled, Hit }
import model.actors.BulletMessages.{ BalloonHit, BulletKilled }
import model.actors.SpawnerMessages.StartRound
import model.actors.TowerMessages.{ Boost, TowerBoosted }
import model.actors.{ BalloonActor, BulletActor, SpawnerActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.maps.Cells.Cell
import model.maps.Tracks.Track
import model.spawn.SpawnManager.Streak
import model.spawn.SpawnerMonad.{ add, RichIO }
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

case class EntityActor(actorRef: ActorRef[Update], entity: Entity)

/**
 * Model of the application, fundamental in the MVC pattern. It receives [[Update]] messages from
 * the game loop and updates the actors governing game entities.
 */
object Model {

  object ModelMessages {
    case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input]) extends Update
    case class NewMap(replyTo: ActorRef[Input]) extends Update
    case class WalletQuantity(replyTo: ActorRef[Input]) extends Update
    case class Pay(amount: Int) extends Update

    case class UpdateEntity(elapsedTime: Double, entities: List[Entity], replyTo: ActorRef[Update])
        extends Update
    case class EntityUpdated(entity: Entity, ref: ActorRef[Update]) extends Update
    case class SpawnEntity(entity: Entity) extends Update
    case class EntitySpawned(entity: Entity, actor: ActorRef[Update]) extends Update
    case class ExitedBalloon(balloon: Balloon, actorRef: ActorRef[Update]) extends Update
    case class TowerIn(cell: Cell) extends Update
  }

  object ModelActor {

    def apply(controller: ActorRef[Input]): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx, controller).init()
    }
  }

  /**
   * The model actor has three behaviors:
   *   - init, in which it just starts a new game by creating a map and a spawner;
   *   - running, in which it waits for a [[TickUpdate]] from the game loop to update the game
   *     entities;
   *   - updating, in which waits for entities to be updated and then notifies the game loop about
   *     it.
   */
  case class ModelActor private (
      ctx: ActorContext[Update],
      controller: ActorRef[Input],
      stats: GameStats = GameStats(),
      var entities: List[EntityActor] = List(),
      var track: Track = Track(),
      var spawner: Option[ActorRef[Update]] = None) {

    def init(): Behavior[Update] = Behaviors.receiveMessage { case NewMap(replyTo) =>
      track = Track(gameGrid)
      spawner = Some(ctx.spawnAnonymous(SpawnerActor(ctx.self, track)))
      replyTo ! MapCreated(track)
      spawner.get ! StartRound {
        (for {
          _ <- add(Streak(10) :- Red)
        } yield ()).get
      }
      running()
    }

    def running(): Behavior[Update] =
      Behaviors.receiveMessage {
        case SpawnEntity(entity) =>
          ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
          Behaviors.same

        case EntitySpawned(entity, actor) =>
          entities = EntityActor(actor, entity) :: entities
          Behaviors.same

        case TowerIn(cell) =>
          val tower: Option[Tower[Bullet]] = entities
            .map(_.entity)
            .collectFirst {
              case tower: Tower[Bullet] if tower.position == cell.centralPosition => tower
            }
          controller ! TowerOption(tower)
          Behaviors.same

        case TickUpdate(elapsedTime, replyTo) =>
          entities.map(_.actorRef) foreach {
            _ ! UpdateEntity(elapsedTime, entities.map(_.entity), ctx.self)
          }
          updating(replyTo)

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        case BoostTowerIn(cell, powerUp) =>
          entities.collect {
            case EntityActor(actorRef, entity) if cell.contains(entity.position) =>
              actorRef
          }.head ! Boost(powerUp, ctx.self)
          Behaviors.same

        case TowerBoosted(tower, actorRef) =>
          controller ! TowerBoosted(tower, actorRef)
          entities = entities.filter(_.actorRef != actorRef).appended(EntityActor(actorRef, tower))
          Behaviors.same

        case BalloonKilled(actorRef) =>
          entities = entities.filter(_.actorRef != actorRef)
          Behaviors.same

        case _ => Behaviors.same
      }

    def updating(
        replyTo: ActorRef[Input],
        updatedEntities: List[EntityActor] = List()): Behavior[Update] =
      Behaviors.receiveMessage {
        case EntityUpdated(entity, ref) =>
          EntityActor(ref, entity) :: updatedEntities match {
            case full if full.size == entities.size =>
              val (balloons, others): (List[Entity], List[Entity]) =
                full.map(_.entity).partition(_.isInstanceOf[Balloon])
              replyTo ! ModelUpdated(
                others.appendedAll(balloons.asInstanceOf[List[Balloon]].sorted),
                stats
              )
              entities = full
              running()
            case notFull => updating(replyTo, notFull)
          }

        case SpawnEntity(entity) =>
          ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
          Behaviors.same

        case EntitySpawned(entity, actor) =>
          entities = EntityActor(actor, entity) :: entities
          ctx.self ! EntityUpdated(entity, actor)
          updating(replyTo, updatedEntities)

        case ExitedBalloon(balloon, actorRef) =>
          stats lose balloon.life
          ctx.self ! BalloonKilled(actorRef)
          Behaviors.same

        case BoostTowerIn(cell, powerUp) =>
          entities.collect {
            case EntityActor(actorRef, entity) if cell.contains(entity.position) =>
              actorRef
          }.head ! Boost(powerUp, ctx.self)
          Behaviors.same

        case TowerBoosted(tower, actorRef) =>
          controller ! TowerBoosted(tower, actorRef)
          entities = entities.filter(_.actorRef != actorRef).appended(EntityActor(actorRef, tower))
          Behaviors.same

        case BulletKilled(actorRef) =>
          killEntity(updatedEntities, replyTo, actorRef)

        case BalloonKilled(actorRef) =>
          if (updatedEntities.map(_.actorRef).contains(actorRef)) {
            entities = entities.filter(_.actorRef != actorRef)
            updating(replyTo, updatedEntities.filter(_.actorRef != actorRef))
          } else {
            killEntity(updatedEntities, replyTo, actorRef)
          }

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        case BalloonHit(bullet, balloons) =>
          entities.filter(e => balloons.contains(e.entity)).foreach {
            _.actorRef ! Hit(bullet, ctx.self)
          }
          Behaviors.same

        case _ => Behaviors.same
      }

    def killEntity(
        updatedEntities: List[EntityActor],
        replyTo: ActorRef[Input],
        actorRef: ActorRef[Update]): Behavior[Update] = updatedEntities match {
      case full if full.size == entities.size - 1 =>
        replyTo ! ModelUpdated(full.map(_.entity), stats)
        entities = full
        running()
      case notFull =>
        entities = entities.filter(_.actorRef != actorRef)
        updating(replyTo, notFull)
    }
  }

  def entitySpawned(entity: Entity, ctx: ActorContext[Update]): ActorRef[Update] = entity match {
    case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
    case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
    case bullet: Bullet   => ctx.spawnAnonymous(BulletActor(bullet))
  }

}
