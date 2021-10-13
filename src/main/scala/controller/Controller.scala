package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.Scheduler
import akka.util.Timeout
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopActor
import controller.GameLoop.GameLoopMessages.Start
import controller.Messages._
import model.Model.ModelActor
import model.Model.ModelMessages.{ Pay, SpawnEntity, WalletQuantity }
import model.actors.TowerMessages.TowerBoosted
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.entities.towers.PowerUps.TowerPowerUp
import model.maps.Cells.Cell

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
    case class NewGame() extends Input
    case class PauseGame() extends Input
    case class ResumeGame() extends Input
    case class StartNextRound() extends Input with Update
    case class NewTimeRatio(value: Double) extends Input
    case class PlaceTower[B <: Bullet](cell: Cell, towerType: TowerType[B]) extends Input
    case class CurrentWallet(amount: Int) extends Input
    case class TowerOption(tower: Option[Tower[Bullet]]) extends Input with Update
    case class BoostTowerIn(cell: Cell, powerUp: TowerPowerUp) extends Input with Update
    case class StartAnimation(entity: Entity) extends Input with Render

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
      case NewGame() =>
        if (gameLoop.isEmpty) {
          model = Some(ctx.spawn(ModelActor(ctx.self), "model"))
          val actor: ActorRef[Input] = ctx.spawn(GameLoopActor(model.get, view), "gameLoop")
          gameLoop = Some(actor)
        }
        gameLoop.get ! Start()
        Behaviors.same

      case ActorInteraction(replyTo, message) =>
        model.get ! message.asInstanceOf[Update]
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

      case StartAnimation(entity) =>
        view ! StartAnimation(entity)
        Behaviors.same

      case _ => Behaviors.same
    }

    def interacting(replyTo: ActorRef[Message]): Behavior[Input] = Behaviors.receiveMessage {
      case TowerOption(tower) =>
        replyTo ! TowerOption(tower)
        default()

      case TowerBoosted(tower, actorRef) =>
        replyTo ! TowerBoosted(tower, actorRef)
        default()

      /*
        EVERYTHING SHOULD BE RESUMED WITH THIS HANDLER

      case message: Message =>
        replyTo ! message
        default()

       */

      case _ => Behaviors.same
    }
  }
}
