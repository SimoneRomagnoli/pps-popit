package model

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages
import controller.Messages.{
  EntityUpdated,
  MapCreated,
  ModelUpdated,
  NewMap,
  TickUpdate,
  Update,
  UpdateEntity
}
import model.actors.{ BalloonActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.BalloonType.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.towers.Towers.{ BaseTower, Tower }
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import utils.Constants

object Model {

  object ModelActor {

    def apply(): Behavior[Update] = Behaviors setup init

    def init(ctx: ActorContext[Update]): Behavior[Update] = Behaviors.receiveMessage {
      case NewMap(replyTo) =>
        val grid: Grid = Grid(Constants.widthRatio, Constants.heightRatio)
        val track: Track = Track(grid)
        replyTo ! MapCreated(track)
        val towerCell: Cell = GridCell(track.start.x, track.start.y - 1)
        val entities: List[Entity] = List(BaseTower() in towerCell, (Red balloon) on track)
        val actors: Seq[ActorRef[Update]] = entities map {
          case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
          case tower: Tower     => ctx.spawnAnonymous(TowerActor(tower))
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
        track: Track): Behavior[Update] = {
      var updatedEntities: List[Entity] = List()
      Behaviors.receiveMessage {
        case EntityUpdated(entity) =>
          updatedEntities = updatedEntities appended entity
          updatedEntities match {
            case full if full.size == entities.size =>
              replyTo ! ModelUpdated(updatedEntities)
              running(ctx, updatedEntities, actors, track)
            case _ => Behaviors.same
          }
        case _ => Behaviors.same
      }
    }
  }

}
