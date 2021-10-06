package controller

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.Messages._
import model.Model.ModelActor
import model.actors.TowerActor
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower

import scala.language.postfixOps

object Controller {

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      ControllerActor(ctx, view, ctx.spawn(ModelActor(ctx.self), "model")).default()
    }
  }

  case class ControllerActor private (
      ctx: ActorContext[Input],
      view: ActorRef[Render],
      var model: ActorRef[Update],
      var gameLoops: Seq[ActorRef[Input]] = Seq()) {
    private def gameLoop: () => ActorRef[Input] = () => gameLoops.head

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case NewGame() =>
        val actor: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")
        gameLoops = gameLoops :+ actor
        gameLoop() ! Start()
        Behaviors.same

      case MvcInteraction(replyTo, message) =>
        model ! message.asInstanceOf[Update]
        interacting(replyTo)

      case PlaceTower(cell, towerType) =>
        val tower: Tower[Bullet] = towerType.tower in cell
        model ! EntitySpawned(tower, ctx.spawnAnonymous(TowerActor(tower)))
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
