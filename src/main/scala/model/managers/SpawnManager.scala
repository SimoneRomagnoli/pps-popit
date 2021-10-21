package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.StartNextRound
import controller.GameLoop.GameLoopMessages.CanStartNextRound
import controller.Messages.{ Input, SpawnManagerMessage, Update, WithReplyTo }
import model.Model.ModelMessages.TrackChanged
import model.actors.BalloonActor
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.managers.EntitiesMessages.EntitySpawned
import model.managers.SpawnerMessages.{ IsRoundOver, RoundOver, RoundStatus, SpawnTick, StartRound }
import model.maps.Tracks.Track
import model.spawn.Rounds.{ Round, Streak }
import model.spawn.RoundsFactory

import scala.language.postfixOps

object SpawnerMessages {
  case class StartRound(round: Round) extends Update with SpawnManagerMessage
  case object SpawnTick extends Update with SpawnManagerMessage
  case class RoundOver(actorRef: ActorRef[Input]) extends Update with SpawnManagerMessage
  case class IsRoundOver() extends Update with SpawnManagerMessage
  case class RoundStatus(on: Boolean) extends Input
}

/**
 * The actor responsible of spawning new [[Balloon]] s.
 */
object SpawnManager {

  def apply(model: ActorRef[Update]): Behavior[Update] = Behaviors.setup { ctx =>
    Spawner(ctx, model).waiting()
  }
}

/**
 * The [[SpawnManager]] related class, conforming to a common Akka pattern.
 *
 * @param ctx
 *   The actor's context.
 * @param model
 *   The model to notify every time a [[Balloon]] is spawned.
 * @param track
 *   The [[Track]] the [[Balloon]] s are gonna follow.
 */
case class Spawner private (
    ctx: ActorContext[Update],
    model: ActorRef[Update],
    var track: Track = Track()) {

  def waiting(): Behavior[Update] = Behaviors.receiveMessage {
    case StartNextRound() =>
      ctx.self ! StartRound(RoundsFactory.nextRound())
      Behaviors.same

    case StartRound(round) =>
      spawningRound(round.streaks)

    case TrackChanged(newTrack) =>
      track = newTrack
      Behaviors.same

    case RoundOver(actorRef) =>
      actorRef ! CanStartNextRound()
      Behaviors.same

    case WithReplyTo(msg, replyTo) =>
      msg match {
        case IsRoundOver() =>
          replyTo ! RoundStatus(true)
      }
      Behaviors.same

    case _ => Behaviors.same
  }

  /**
   * Spawns a round made up of:
   * @param streaks
   *   The remaining [[Streak]] s to spawn.
   * @return
   *   The [[Behavior]] that is gonna spawn the next [[Streak]].
   */
  private def spawningRound(streaks: Seq[Streak]): Behavior[Update] = streaks match {
    case h :: t =>
      Behaviors.withTimers { timers =>
        timers.startTimerWithFixedDelay(SpawnTick, h.interval)
        spawningStreak(
          LazyList
            .iterate((h.balloonInfo.balloonLife balloon) adding h.balloonInfo.balloonTypes)(b => b)
            .map(_ on track)
            .take(h.quantity),
          t
        )
      }
    case _ => waiting()
  }

  /** Spawns a new streak. */
  private def spawningStreak(streak: LazyList[Balloon], later: Seq[Streak]): Behavior[Update] =
    Behaviors.receiveMessage {
      case SpawnTick =>
        streak match {
          case h #:: t =>
            model ! EntitySpawned(h, ctx.spawnAnonymous(BalloonActor(h)))
            spawningStreak(t, later)
          case _ =>
            Behaviors.withTimers { timers =>
              timers.cancel(SpawnTick)
              spawningRound(later)
            }
        }

      case WithReplyTo(msg, replyTo) =>
        msg match {
          case IsRoundOver() =>
            replyTo ! RoundStatus(false)
        }
        Behaviors.same

      case _ => Behaviors.same
    }
}
