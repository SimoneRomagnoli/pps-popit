package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.util.Timeout
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopActor
import controller.GameLoop.GameLoopMessages.{ MapCreated, Start, Stop }
import controller.Messages._
import model.Model.ModelActor
import model.entities.Entities.Entity
import model.managers.EntitiesMessages.PlaceTower
import model.managers.GameDynamicsMessages.{ CurrentGameTrack, CurrentTrack, NewMap }
import model.maps.Tracks.Track
import utils.Futures.retrieve
import view.View.ViewMessages.RenderMap

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Controller of the application, fundamental in the MVC pattern. It deals with inputs coming from
 * the view controllers.
 */
object Controller {

  object ControllerMessages {
    case class NewGame(withTrack: Option[Track]) extends Input with Render
    case class ExitGame() extends Input with Render
    case class PauseGame() extends Input with SpawnManagerMessage
    case class ResumeGame() extends Input with SpawnManagerMessage
    case class RestartGame() extends Input
    case class NewTrack() extends Input

    case class StartNextRound()
        extends Input
        with SpawnManagerMessage
        with GameDynamicsManagerMessage
        with EntitiesManagerMessage
    case class NewTimeRatio(value: Double) extends Input
    case class CurrentWallet(amount: Int) extends Input
    case class StartAnimation(entity: Entity) extends Render

    sealed trait Interaction extends Input {
      def replyTo: ActorRef[Message]
      def request: Message
    }

    case class ActorInteraction(
        override val replyTo: ActorRef[Message],
        override val request: Message)
        extends Interaction
  }

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      ControllerActor(ctx, view).default()
    }
  }

  /**
   * The controller actor has two behaviors:
   *   - default, in which it simply receives input messages and satisfies them;
   *   - interacting, in which it has to respond to another subscribed actor that needs a response
   *     (mostly the view requiring information from the model).
   */
  case class ControllerActor private (
      ctx: ActorContext[Input],
      view: ActorRef[Render],
      var model: Option[ActorRef[Update]] = None,
      var gameLoop: Option[ActorRef[Input]] = None) {
    implicit val timeout: Timeout = Timeout(1.seconds)
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case NewGame(withTrack) =>
        view ! NewGame(withTrack)
        model = Some(ctx.spawnAnonymous(ModelActor()))
        gameLoop = Some(ctx.spawnAnonymous(GameLoopActor(model.get, view)))
        model.get ! NewMap(ctx.self, withTrack)
        gameLoop.get ! Start()
        Behaviors.same

      case NewTrack() =>
        model.get ! NewMap(ctx.self, None)
        Behaviors.same

      case MapCreated(track) =>
        view ! RenderMap(track)
        Behaviors.same

      case RestartGame() =>
        retrieve(model.get ? CurrentGameTrack) {
          case CurrentTrack(track) =>
            gameLoop.get ! Stop()
            model.get ! Stop()
            gameLoop = None
            model = None
            ctx.self ! NewGame(Some(track))
          case _ =>
        }
        Behaviors.same

      case ExitGame() =>
        view ! ExitGame()
        model.get ! Stop()
        gameLoop.get ! Stop()
        gameLoop = None
        model = None
        Behaviors.same

      case ActorInteraction(replyTo, message) =>
        model.get ! WithReplyTo(message.asInstanceOf[Update], ctx.self)
        interacting(replyTo)

      case StartNextRound() =>
        model.get ! StartNextRound()
        Behaviors.same

      case PlaceTower(cell, towerType) =>
        model.get ! WithReplyTo(PlaceTower(cell, towerType), ctx.self)
        Behaviors.same

      case input: Input if input.isInstanceOf[PauseGame] || input.isInstanceOf[ResumeGame] =>
        gameLoop.get ! input
        Behaviors.same

      case _ => Behaviors.same
    }

    def interacting(replyTo: ActorRef[Message]): Behavior[Input] = Behaviors.receiveMessage {
      message =>
        replyTo ! message
        default()
    }
  }
}
