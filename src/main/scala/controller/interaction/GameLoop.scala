package controller.interaction

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.{ PauseGame, ResumeGame, StartAnimation }
import Messages.{ Input, Render, Update }
import controller.interaction.GameLoop.GameLoopMessages._
import controller.settings.Settings.Time.{ delay, elapsedTime, TimeSettings }
import model.Model.ModelMessages.TickUpdate
import model.entities.Entities.Entity
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import view.View.ViewMessages.{ RenderEntities, RenderGameOver, RenderStats }

import scala.concurrent.duration.DurationDouble

/**
 * Game Loop of the application; it represents the control flow of a game. It sends [[Update]]
 * messages to the model and [[Render]] messages to the view, performing a fundamental duty in the
 * MVC pattern.
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

    def apply(
        model: ActorRef[Update],
        view: ActorRef[Render],
        timeSettings: TimeSettings): Behavior[Input] = Behaviors.setup { ctx =>
      Behaviors.receiveMessagePartial {
        case Start() =>
          Behaviors.withTimers { timers =>
            timers.startTimerWithFixedDelay(Tick, delay(timeSettings.frameRate).seconds)
            GameLoopActor(ctx, model, view, timeSettings).running()
          }
      }
    }
  }

  /**
   * The game loop actor.
   */
  case class GameLoopActor private (
      ctx: ActorContext[Input],
      model: ActorRef[Update],
      view: ActorRef[Render],
      timeSettings: TimeSettings) {

    /**
     * Here the [[GameLoop]] waits for a [[Tick]] message in order to update the model and then
     * waits for its response to render the view. It also receives notifications about updated
     * [[GameStats]].
     */
    private def running(): Behavior[Input] = Behaviors.receiveMessagePartial {
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
    }

    /**
     * The model and the view are not updated anymore, the [[GameLoop]] is waiting for a message to
     * resume the game.
     */
    private def paused(): Behavior[Input] = Behaviors.receiveMessagePartial {
      case ResumeGame() =>
        model ! ResumeGame()
        running()
    }
  }
}
