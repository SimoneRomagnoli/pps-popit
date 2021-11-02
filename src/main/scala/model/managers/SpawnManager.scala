package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.{ PauseGame, ResumeGame, StartNextRound }
import controller.interaction.Messages.{ Input, SpawnManagerMessage, Update }
import controller.settings.Settings.Time.TimeSettings
import model.Model.ModelMessages.TrackChanged
import model.actors.BalloonActor
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.managers.EntitiesMessages.{ DoneSpawning, EntitySpawned }
import model.managers.SpawnerMessages.{ SpawnTick, StartRound }
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

  def apply(model: ActorRef[Update], timeSettings: TimeSettings): Behavior[Update] =
    Behaviors.setup { ctx =>
      RoundsFactory.startGame()
      Spawner(ctx, model, timeSettings).waiting()
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
    timeSettings: TimeSettings,
    var track: Track = Track()) {

  def waiting(): Behavior[Update] = Behaviors.receiveMessagePartial {
    case StartNextRound() =>
      ctx.self ! StartRound(RoundsFactory.nextRound())
      Behaviors.same

    case StartRound(round) =>
      spawningRound(round.streaks)

    case TrackChanged(newTrack) =>
      track = newTrack
      Behaviors.same
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
        timers.startTimerWithFixedDelay(SpawnTick, h.interval / timeSettings.timeRatio.toLong)
        spawningStreak(
          LazyList
            .iterate((h.balloonInfo.balloonLife balloon) adding h.balloonInfo.balloonTypes)(b => b)
            .map(_ on track)
            .map(_ in track.start)
            .take(h.quantity),
          t
        )
      }
    case _ =>
      model ! DoneSpawning()
      waiting()
  }

  /** Spawns a new streak. */
  private def spawningStreak(streak: LazyList[Balloon], later: Seq[Streak]): Behavior[Update] =
    Behaviors.receiveMessagePartial {
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

      case PauseGame() =>
        paused(streak, later)
    }

  def paused(streak: LazyList[Balloon], later: Seq[Streak]): Behavior[Update] =
    Behaviors.receiveMessagePartial { case ResumeGame() =>
      spawningStreak(streak, later)
    }
}
