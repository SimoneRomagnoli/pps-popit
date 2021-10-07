package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.util.Timeout
import controller.GameLoop.GameLoopActor
import controller.Messages.{
  CurrentWallet,
  EntitySpawned,
  Input,
  NewGame,
  PauseGame,
  Pay,
  PlaceTower,
  Render,
  ResumeGame,
  Start,
  Update,
  WalletQuantity
}
import model.Model.ModelActor
import model.actors.TowerActor
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower

import scala.concurrent.{ Await, ExecutionContextExecutor }
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Controller {

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      val model: ActorRef[Update] = ctx.spawn(ModelActor(), "model")
      var gameLoops: Seq[ActorRef[Input]] = Seq()
      val gameLoop: () => ActorRef[Input] = () => gameLoops.head

      implicit val timeout: Timeout = Timeout(1.seconds)
      implicit val scheduler: Scheduler = ctx.system.scheduler
      implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

      Behaviors.receiveMessage {
        case NewGame() =>
          val actor: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")
          gameLoops = gameLoops :+ actor
          gameLoop() ! Start()
          Behaviors.same

        case PlaceTower(cell, towerType) =>
          val future = model.ask(WalletQuantity)
          val amount: Int = Await.result(future, timeout.duration) match {
            case CurrentWallet(v) => v
          }

          if (amount >= towerType.cost) {
            val tower: Tower[Bullet] = towerType.tower in cell
            model ! EntitySpawned(tower, ctx.spawnAnonymous(TowerActor(tower)))
            model ! Pay(towerType.cost)
          }
          Behaviors.same

        case input: Input if input.isInstanceOf[PauseGame] || input.isInstanceOf[ResumeGame] =>
          gameLoop() ! input
          Behaviors.same

        case _ => Behaviors.same
      }
    }
  }
}
