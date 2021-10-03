package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderEntities, RenderMap }
import model.entities.Entities.Entity
import utils.Constants.Maps.gameGrid
import view.controllers.{ ViewGameController, ViewMainController }

import scala.language.reflectiveCalls

object View {

  object ViewActor {

    def apply(mainController: ViewMainController): Behavior[Render] = Behaviors.setup { _ =>
      new ViewActor(mainController).inGame(mainController.gameController)
    }
  }

  class ViewActor private (mainController: ViewMainController) {

    private def inGame(gameController: ViewGameController): Behavior[Render] =
      Behaviors.receiveMessage {
        case RenderEntities(entities: List[Entity]) =>
          gameController draw entities
          Behaviors.same

        case RenderMap(track) =>
          gameController.reset()
          gameController draw gameGrid
          gameController draw track
          Behaviors.same

        case _ => Behaviors.same
      }
  }

}
