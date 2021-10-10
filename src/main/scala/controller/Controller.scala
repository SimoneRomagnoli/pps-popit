package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.Scheduler
import akka.util.Timeout
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.Messages._
import model.Model.ModelActor
import model.actors.TowerActor
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{ Failure, Success }

/**
 * Controller of the application, fundamental in the MVC pattern. It deals with inputs coming from
 * the view controllers.
 */
object Controller {

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      ControllerActor(ctx, view, ctx.spawn(ModelActor(ctx.self), "model")).default()
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
      var model: ActorRef[Update],
      var gameLoops: Seq[ActorRef[Input]] = Seq()) {
    private def gameLoop: () => ActorRef[Input] = () => gameLoops.head
    implicit val timeout: Timeout = Timeout(1.seconds)
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case NewGame() =>
        val actor: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")
        gameLoops = gameLoops :+ actor
        gameLoop() ! Start()
        Behaviors.same

      case MvcInteraction(replyTo, message) =>
        model ! message.asInstanceOf[Update]
        interacting(replyTo)

      case BoostTowerIn(cell, powerUp) =>
        model ask WalletQuantity onComplete {
          case Success(value) =>
            value match {
              case CurrentWallet(amount) =>
                if (amount >= powerUp.cost) {
                  model ! BoostTowerIn(cell, powerUp)
                }
            }
          case Failure(exception) => println(exception)
        }
        Behaviors.same

      case PlaceTower(cell, towerType) =>
        model ? WalletQuantity onComplete {
          case Success(value) =>
            value match {
              case CurrentWallet(amount) =>
                if (amount >= towerType.cost) {
                  val tower: Tower[Bullet] = towerType.tower in cell
                  model ! SpawnEntity(tower)
                  model ! Pay(towerType.cost)
                }
            }
          case Failure(exception) => println(exception)
        }
        Behaviors.same

      case input: Input if input.isInstanceOf[PauseGame] || input.isInstanceOf[ResumeGame] =>
        gameLoop() ! input
        Behaviors.same

      case _ => Behaviors.same
    }

    def interacting(replyTo: ActorRef[Message]): Behavior[Input] = Behaviors.receiveMessage {
      case TowerOption(tower) =>
        replyTo ! TowerOption(tower)
        default()

      case _ => Behaviors.same
    }
  }
}
