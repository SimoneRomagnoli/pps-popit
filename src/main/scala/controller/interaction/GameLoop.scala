package controller.interaction

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import controller.Controller.ControllerMessages.{PauseGame, ResumeGame, StartAnimation}
import Messages.{Input, Render, Update}
import controller.interaction.GameLoop.GameLoopMessages._
import controller.settings.Settings.Time.{TimeSettings, delay, elapsedTime, frameRate, timeRatio}
import model.Model.ModelMessages.TickUpdate
import model.entities.Entities.Entity
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import view.View.ViewMessages.{RenderEntities, RenderGameOver, RenderStats}

import scala.concurrent.duration.DurationDouble

/**
 * Game Loop of the application; it represents the flow of a game. It sends [[Update]] messages to a
 * model and [[Render]] messages to a view, performing a fundamental duty in the MVC pattern.
 */
object GameLoop {

  object GameLoopMessages {
    case object Tick extends Input
    case class Start() extends Input
    case class GameOver() extends Input
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

    def apply(model: ActorRef[Update], view: ActorRef[Render], timeSettings: TimeSettings): Behavior[Input] = Behaviors.setup {
      ctx =>
        Behaviors.receiveMessage {
          case Start() =>
            Behaviors.withTimers { timers =>
              timers.startTimerWithFixedDelay(Tick, delay(frameRate).seconds)
              GameLoopActor(ctx, model, view, timeSettings).running()
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
      timeSettings: TimeSettings) {

    private def running(): Behavior[Input] = Behaviors.receiveMessage {
      case Tick =>
        model ! TickUpdate(elapsedTime(timeSettings.frameRate)(timeSettings.timeRatio), ctx.self)
        Behaviors.same

      case ModelUpdated(entities, animations) =>
        view ! RenderEntities(entities)
        animations.foreach {
          view ! StartAnimation(_)
        }
        Behaviors.same

      case PauseGame() =>
        model ! PauseGame()
        paused()

      case GameOver() =>
        view ! RenderGameOver()
        ctx.self ! Stop()
        model ! Stop()
        Behaviors.same

      case Stop() =>
        Behaviors.stopped

      case GameStatsUpdated(stats) =>
        view ! RenderStats(stats)
        Behaviors.same

      case CanStartNextRound() =>
        view ! CanStartNextRound()
        Behaviors.same

      case _ => Behaviors.same
    }

    private def paused(): Behavior[Input] = Behaviors.receiveMessage {
      case ResumeGame() =>
        model ! ResumeGame()
        running()
      case _ => Behaviors.same
    }
  }
}
