package controller

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopMessages._
import controller.GameLoop.Time._
import controller.Messages._
import model.Model.ModelMessages.TickUpdate
import model.entities.Entities.Entity
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import view.View.ViewMessages._

import scala.concurrent.duration.DurationDouble

/**
 * Game Loop of the application; it represents the flow of a game. It sends [[Update]] messages to a
 * model and [[Render]] messages to a view, performing a fundamental duty in the MVC pattern.
 */
object GameLoop {

  object GameLoopMessages {
    case object Tick extends Input
    case class Start() extends Input
    case class Stop() extends Input with Update
    case class MapCreated(track: Track) extends Input
    case class ModelUpdated(entities: List[Entity], animations: List[Entity]) extends Input
    case class CanStartNextRound() extends Input with Render
    case class GameStatsUpdated(stats: GameStats) extends Input with Render
  }

  /**
   * The loop is obtained by exploiting the reactivity of an actor: the actor just sends a [[Tick]]
   * message to itself with a determined delay that depends on the specified frame rate.
   */
  object GameLoopActor {

    def apply(model: ActorRef[Update], view: ActorRef[Render]): Behavior[Input] = Behaviors.setup {
      ctx =>
        Behaviors.receiveMessage {
          case Start() =>
            Behaviors.withTimers { timers =>
              timers.startTimerWithFixedDelay(Tick, delay(frameRate).seconds)
              GameLoopActor(ctx, model, view).running()
            }
          case _ => Behaviors.same
        }
    }
  }

  /**
   * The game loop actor has two behaviors:
   *   - running, in which it simply waits for a [[Tick]] message to update the model and waits for
   *     its response to render the view;
   *   - paused, in which it waits for the game to be resumed.
   */
  case class GameLoopActor private (
      ctx: ActorContext[Input],
      model: ActorRef[Update],
      view: ActorRef[Render],
      var timeRatio: Double = 1.0) {

    private def running(): Behavior[Input] = Behaviors.receiveMessage {
      case Tick =>
        model ! TickUpdate(elapsedTime(frameRate)(timeRatio), ctx.self)
        Behaviors.same

      case ModelUpdated(entities, animations) =>
        view ! RenderEntities(entities)
        animations.foreach {
          view ! StartAnimation(_)
        }
        Behaviors.same

      case PauseGame() =>
        paused()

      case Stop() =>
        Behaviors.stopped

      case GameStatsUpdated(stats) =>
        view ! RenderStats(stats)
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
