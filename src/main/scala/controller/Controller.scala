package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.util.Timeout
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopActor
import controller.GameLoop.GameLoopMessages.{ MapCreated, Start, Stop }
import controller.Messages._
import controller.TrackLoader.TrackLoaderActor
import controller.TrackLoader.TrackLoaderMessages.SaveActualTrack
import model.Model.ModelActor
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.PowerUps.TowerPowerUp
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.SpawnEntity
import model.managers.GameDynamicsMessages.{ NewMap, Pay, WalletQuantity }
import model.maps.Cells.Cell
import view.View.ViewMessages.RenderMap

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
 * Controller of the application, fundamental in the MVC pattern. It deals with inputs coming from
 * the view controllers.
 */
object Controller {

  object ControllerMessages {
    case class NewGame() extends Input with Render
    case class ExitGame() extends Input with Render
    case class FinishGame() extends Input with Render
    case class HighScoresPage() extends Input with Render
    case class PauseGame() extends Input
    case class ResumeGame() extends Input
    case class NewTrack() extends Input
    case class StartNextRound() extends Input with SpawnManagerMessage
    case class NewTimeRatio(value: Double) extends Input
    case class PlaceTower[B <: Bullet](cell: Cell, towerType: TowerType[B]) extends Input
    case class CurrentWallet(amount: Int) extends Input
    case class BoostTowerIn(cell: Cell, powerUp: TowerPowerUp) extends Input with Update
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
      var gameLoop: Option[ActorRef[Input]] = None,
      var trackLoader: Option[ActorRef[Input]] = None) {
    implicit val timeout: Timeout = Timeout(1.seconds)
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case NewGame() =>
        view ! NewGame()
        if (gameLoop.isEmpty) {
          model = Some(ctx.spawnAnonymous(ModelActor(ctx.self)))
          gameLoop = Some(ctx.spawnAnonymous(GameLoopActor(model.get, view)))
          trackLoader = Some(ctx.spawnAnonymous(TrackLoaderActor()))
        }
        model.get ! NewMap(ctx.self)
        gameLoop.get ! Start()
        Behaviors.same

      case HighScoresPage() =>
        view ! HighScoresPage()
        Behaviors.same

      case NewTrack() =>
        model.get ! NewMap(ctx.self)
        Behaviors.same

      case MapCreated(track) =>
        trackLoader.get ! SaveActualTrack(track)
        view ! RenderMap(track)

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

      case BoostTowerIn(cell, powerUp) =>
        model.get ask WalletQuantity onComplete {
          case Success(value) =>
            value match {
              case CurrentWallet(amount) =>
                if (amount >= powerUp.cost) {
                  model.get ! BoostTowerIn(cell, powerUp)
                }
            }
          case Failure(exception) => println(exception)
        }
        Behaviors.same

      case PlaceTower(cell, towerType) =>
        model.get ? WalletQuantity onComplete {
          case Success(value) =>
            value match {
              case CurrentWallet(amount) =>
                if (amount >= towerType.cost) {
                  val tower: Tower[Bullet] = towerType.tower in cell
                  model.get ! SpawnEntity(tower)
                  model.get ! Pay(towerType.cost)
                }
            }
          case Failure(exception) => println(exception)
        }
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
