package model

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages
import controller.Messages.{
  EntitySpawned,
  EntityUpdated,
  MapCreated,
  ModelUpdated,
  NewMap,
  TickUpdate,
  Update,
  UpdateEntity
}
import model.actors.{ BalloonActor, BulletActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.BalloonType.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Dart
import model.entities.towers.TowerTypes.Monkey
import model.entities.towers.Towers.Tower
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Track
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

object Model {

  object ModelActor {

    def apply(): Behavior[Update] = Behaviors setup init

    def init(ctx: ActorContext[Update]): Behavior[Update] = Behaviors.receiveMessage {
      case NewMap(replyTo) =>
        val track: Track = Track(gameGrid)
        replyTo ! MapCreated(track)
        val towerCell: Cell = GridCell(track.start.x, track.start.y - 1)
        val entities: List[Entity] =
          List((Monkey tower) in towerCell, (Red balloon) on track)
        val actors: Seq[ActorRef[Update]] = entities map {
          case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
          case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
          case dart: Dart       => ctx.spawnAnonymous(BulletActor(dart))
        }
        running(ctx, entities, actors, track)
    }

    def running(
        ctx: ActorContext[Update],
        entities: List[Entity],
        actors: Seq[ActorRef[Update]],
        track: Track): Behavior[Update] =
      Behaviors.receiveMessage { case TickUpdate(elapsedTime, replyTo) =>
        actors foreach {
          _ ! UpdateEntity(elapsedTime, entities, ctx.self, track)
        }
        updating(ctx, entities, actors, replyTo, track)
      }

    def updating(
        ctx: ActorContext[Update],
        entities: List[Entity],
        actors: Seq[ActorRef[Update]],
        replyTo: ActorRef[Messages.Input],
        track: Track,
        updatedEntities: List[Entity] = List()): Behavior[Update] =
      Behaviors.receiveMessage {
        case EntityUpdated(entity) =>
          entity :: updatedEntities match {
            case full if full.size == entities.size =>
              replyTo ! ModelUpdated(full)
              running(ctx, full, actors, track)
            case notFull => updating(ctx, entities, actors, replyTo, track, notFull)
          }
        case EntitySpawned(entity, actor) =>
          updating(
            ctx,
            entity :: entities,
            actors :+ actor,
            replyTo,
            track,
            entity :: updatedEntities
          )
        case _ => Behaviors.same
      }
  }

}
