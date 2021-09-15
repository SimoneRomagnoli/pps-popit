package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.Messages.{ Input, NewGame, Render, Update }
import model.Model.ModelActor

object Controller {

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.receive { (ctx, msg) =>
      msg match {
        case NewGame() =>
          val model: ActorRef[Update] = ctx.spawn(ModelActor(), "model")
          val gameLoop: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")

          gameLoop ! NewGame()
          Behaviors.same

        case _ => Behaviors.same
      }
    }
  }

}
