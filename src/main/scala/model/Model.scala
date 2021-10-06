package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages
import controller.Messages._
import model.actors.{ BalloonActor, BulletActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Dart
import model.entities.towers.Towers.Tower
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

object Model {

  object ModelActor {

    def apply(): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx).init()
    }
  }

  case class ModelActor private (
      ctx: ActorContext[Update],
      stats: GameStats = GameStats(),
      var entities: List[Entity] = List(),
      var actors: Seq[ActorRef[Update]] = Seq(),
      var track: Track = Track()) {

    def init(): Behavior[Update] = Behaviors.receiveMessage { case NewMap(replyTo) =>
      track = Track(gameGrid)
      replyTo ! MapCreated(track)
      entities = List((Red balloon) on track at (10.0, 5.0))
      actors = entities map {
        case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
        case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
        case dart: Dart       => ctx.spawnAnonymous(BulletActor(dart))
      }
      running()
    }

    def running(): Behavior[Update] =
      Behaviors.receiveMessage {
        case EntitySpawned(entity, actor) =>
          entities = entity :: entities
          actors = actors :+ actor
          running()

        case TickUpdate(elapsedTime, replyTo) =>
          actors foreach {
            _ ! UpdateEntity(elapsedTime, entities, ctx.self, track)
          }
          updating(replyTo)
      }

    def updating(
        replyTo: ActorRef[Messages.Input],
        updatedEntities: List[Entity] = List()): Behavior[Update] =
      Behaviors.receiveMessage {
        case EntityUpdated(entity) =>
          entity :: updatedEntities match {
            case full if full.size == entities.size =>
              replyTo ! ModelUpdated(full, stats)
              entities = full
              running()
            case notFull => updating(replyTo, notFull)
          }
        case EntitySpawned(entity, actor) =>
          entities = entity :: entities
          actors = actors :+ actor
          updating(replyTo, entity :: updatedEntities)

        case ExitedBalloon(balloon, actorRef) =>
          stats lose balloon.life
          ctx.self ! EntityKilled(balloon, actorRef)
          Behaviors.same

        case EntityKilled(entity, actorRef) =>
          updatedEntities match {
            case full if full.size == entities.size - 1 =>
              replyTo ! ModelUpdated(full, stats)
              entities = full
              actors = actors.filter(_ != actorRef)
              running()
            case notFull =>
              entities = entities.filter(_ not entity)
              actors = actors.filter(_ != actorRef)
              updating(replyTo, notFull)
          }

        case _ => Behaviors.same
      }
  }

}
