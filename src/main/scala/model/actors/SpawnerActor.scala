package model.actors

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ EntitySpawned, Update }
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.spawn.SpawnManager.{ Round, Streak }

import scala.language.postfixOps

case class StartRound(round: Round) extends Update
private case object SpawnTick extends Update

object SpawnerActor {

  def apply(model: ActorRef[Update]): Behavior[Update] = Behaviors.setup { ctx =>
    Spawner(ctx, model).waiting()
  }
}

case class Spawner private (ctx: ActorContext[Update], model: ActorRef[Update]) {

  def waiting(): Behavior[Update] = Behaviors.receiveMessage {
    case StartRound(round) =>
      spawningRound(round.streaks)
    case _ => Behaviors.same
  }

  private def spawningRound(streaks: Seq[Streak]): Behavior[Update] = streaks match {
    case h :: t =>
      Behaviors.withTimers { timers =>
        timers.startTimerWithFixedDelay(SpawnTick, h.interval)
        spawningStreak(
          LazyList
            .iterate((h.balloonInfo.balloonLife balloon) adding h.balloonInfo.balloonTypes)(b => b)
            .take(h.quantity),
          t
        )
      }
    case _ => waiting()
  }

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
