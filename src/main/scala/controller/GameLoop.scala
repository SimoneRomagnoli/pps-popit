package controller

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.Time._
import controller.Messages._

import scala.concurrent.duration.DurationDouble

object GameLoop {

  object GameLoopActor {

    def apply(model: ActorRef[Update], view: ActorRef[Render]): Behavior[Input] = Behaviors.setup {
      ctx =>
        Behaviors.receiveMessage {
          case Start() =>
            model ! NewMap(ctx.self)
            Behaviors.withTimers { timers =>
              timers.startTimerWithFixedDelay(Tick, delay(frameRate).seconds)
              GameLoopActor(ctx, model, view).running()
            }
          case _ => Behaviors.same
        }
    }

    def apply(
        ctx: ActorContext[Input],
        model: ActorRef[Update],
        view: ActorRef[Render]): GameLoopActor =
      new GameLoopActor(ctx, model, view)
  }

  class GameLoopActor private (
      ctx: ActorContext[Input],
      model: ActorRef[Update],
      view: ActorRef[Render],
      var timeRatio: Double = 1.0) {

    private def running(): Behavior[Input] = Behaviors.receiveMessage {
      case Tick =>
        model ! TickUpdate(elapsedTime(frameRate)(timeRatio), ctx.self)
        Behaviors.same
      case MapCreated(grid, track) =>
        view ! RenderMap(grid, track)
        Behaviors.same
      case ModelUpdated(entities) =>
        view ! RenderEntities(entities)
        Behaviors.same
      case PauseGame() =>
        paused()
      case NewTimeRatio(value) =>
        timeRatio = value
        Behaviors.same
      case _ => Behaviors.same
    }

    private def paused(): Behavior[Input] = Behaviors.receiveMessage {
      case ResumeGame() =>
        running()
      case _ => Behaviors.same
    }
  }

  object Time {
    val frameRate: Double = 60.0
    val truncate: Double => Double = n => (n * 1000).round / 1000.toDouble
    val delay: Double => Double = n => truncate(1.0 / n)

    def elapsedTime(frameRate: Double)(implicit timeRatio: Double = 1.0): Double =
      delay(frameRate) * timeRatio
  }
}
