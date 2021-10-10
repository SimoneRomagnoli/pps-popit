package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages
import controller.Messages._
import model.actors.BalloonMessages.Hit
import model.actors.SpawnerMessages.StartRound
import model.actors.{ BalloonActor, BulletActor, SpawnerActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletMessages.BalloonHit
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
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
      var actors: Seq[ActorRef[Update]] = Seq(),
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
          actors = actors :+ actor
          Behaviors.same

        case TowerIn(cell) =>
          val tower: Option[Tower[_]] = entities
            .map(_.entity)
            .find(e => e.isInstanceOf[Tower[_]] && e.position == cell.centralPosition)
            .map(_.asInstanceOf[Tower[_]])
          controller ! TowerOption(tower)
          Behaviors.same

        case TickUpdate(elapsedTime, replyTo) =>
          actors foreach {
            _ ! UpdateEntity(elapsedTime, entities.map(_.entity), ctx.self)
          }
          updating(replyTo)

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        case EntityKilled(_, actorRef) =>
          entities = entities.filter(_.actorRef != actorRef)
          actors = actors.filter(_ != actorRef)
          Behaviors.same

        case _ => Behaviors.same
      }

    def updating(
        replyTo: ActorRef[Messages.Input],
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
          actors = actors :+ actor
          ctx.self ! EntityUpdated(entity, actor)
          updating(replyTo, updatedEntities)

        case ExitedBalloon(balloon, actorRef) =>
          stats lose balloon.life
          ctx.self ! EntityKilled(balloon, actorRef)
          Behaviors.same

        case EntityKilled(entity, actorRef) =>
          updatedEntities match {
            case full if full.size == entities.size - 1 =>
              replyTo ! ModelUpdated(full.map(_.entity), stats)
              entities = full
              actors = actors.filter(_ != actorRef)
              running()
            case notFull =>
              entities = entities.filter(_.actorRef != actorRef)
              actors = actors.filter(_ != actorRef)
              updating(replyTo, notFull)
          }

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        case BalloonHit(bullet, balloon) =>
          entities.find(_.entity == balloon) match {
            case Some(entityActor) =>
              entityActor.actorRef ! Hit(bullet, ctx.self)
            case _ =>
          }
          Behaviors.same

        case _ => Behaviors.same
      }
  }

  def entitySpawned(entity: Entity, ctx: ActorContext[Update]): ActorRef[Update] = entity match {
    case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
    case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
    case bullet: Bullet   => ctx.spawnAnonymous(BulletActor(bullet))
  }

}
