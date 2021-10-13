package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.StartNextRound
import controller.Messages.{ SpawnManagerMessage, Update }
import model.actors.BalloonActor
import model.entities.balloons.BalloonLives.{ Blue, Red }
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.managers.EntitiesMessages.EntitySpawned
import model.managers.SpawnerMessages.{ SpawnTick, StartRound }
import model.maps.Tracks.Track
import model.spawn.RoundBuilders._
import model.spawn.Rounds.{ Round, Streak }

import scala.language.postfixOps

object SpawnerMessages {
  case class StartRound(round: Round) extends Update with SpawnManagerMessage
  case object SpawnTick extends Update with SpawnManagerMessage
}

/**
 * The actor responsible of spawning new [[Balloon]] s.
 */
object SpawnManager {

  def apply(model: ActorRef[Update], track: Track): Behavior[Update] = Behaviors.setup { ctx =>
    Spawner(ctx, model, track, 0).waiting()
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
    track: Track,
    var round: Int) {

  def waiting(): Behavior[Update] = Behaviors.receiveMessage {
    case StartNextRound() =>
      round += 1
      ctx.self ! StartRound {
        (for {
          _ <- add(Streak(round * 10) :- Red)
          _ <- add(Streak(round * 5) :- Blue)
        } yield ()).get
      }
      Behaviors.same
    case StartRound(round) =>
      spawningRound(round.streaks)
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
      case _ => Behaviors.same
    }
}
