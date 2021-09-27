package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderEntities, RenderLoading, RenderMap }
import model.entities.Entities.Entity
import view.controllers.{ ViewController, ViewGameController }

import scala.language.reflectiveCalls

object View {

  object ViewActor {

    def apply(mainController: ViewController): Behavior[Render] = Behaviors.setup { _ =>
      new ViewActor(mainController).inGame(mainController.gameController)
    }
  }

  class ViewActor private (mainController: ViewController) {

    private def inGame(gameController: ViewGameController): Behavior[Render] =
      Behaviors.receiveMessage {
        case RenderLoading() =>
          gameController.loading()
          Behaviors.same

        case RenderEntities(entities: List[Entity]) =>
          gameController draw entities
          Behaviors.same

        case RenderMap(track) =>
          gameController.reset()
          gameController.drawGrid()
          gameController draw track
          Behaviors.same

        case _ => Behaviors.same
      }
  }

}
