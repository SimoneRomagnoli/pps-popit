package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.Messages.{
  EntitySpawned,
  Input,
  NewGame,
  PauseGame,
  PlaceTower,
  Render,
  ResumeGame,
  Start,
  Update
}
import model.Model.ModelActor
import model.actors.TowerActor
import model.entities.bullets.Bullets.Dart
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.Towers.Tower

import scala.language.postfixOps

object Controller {

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      val model: ActorRef[Update] = ctx.spawn(ModelActor(), "model")
      var gameLoops: Seq[ActorRef[Input]] = Seq()
      val gameLoop: () => ActorRef[Input] = () => gameLoops.head
      Behaviors.receiveMessage {
        case NewGame() =>
          val actor: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")
          gameLoops = gameLoops :+ actor
          gameLoop() ! Start()
          Behaviors.same

        case PlaceTower(cell) =>
          val tower: Tower[Dart] = (Arrow tower) in cell
          model ! EntitySpawned(tower, ctx.spawnAnonymous(TowerActor(tower)))
          Behaviors.same

        case input: Input if input.isInstanceOf[PauseGame] || input.isInstanceOf[ResumeGame] =>
          gameLoop() ! input
          Behaviors.same

        case _ => Behaviors.same
      }
    }
  }
}
