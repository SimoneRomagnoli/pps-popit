package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderEntities, RenderLoading, RenderMap }
import model.entities.Entities.Entity
import scalafx.scene.layout.Pane
import view.GameBoard.Board
import view.controllers.ViewController

import scala.language.reflectiveCalls

object View {

  object ViewActor {

    //val board: Board = Board()

    def apply(mainController: ViewController): Behavior[Render] = Behaviors.setup { _ =>
      Behaviors.receiveMessage {
        case RenderLoading() =>
          //board.loading()
          mainController.loading()
          Behaviors.same

        case RenderEntities(entities: List[Entity]) =>
          //board draw entities
          mainController draw entities
          Behaviors.same

        case RenderMap(track) =>
          //board.reset()
          //board.drawGrid()
          //board draw track
          mainController.reset()
          mainController.drawGrid()
          mainController draw track
          Behaviors.same

        case _ => Behaviors.same
      }
    }

    //def delegate: Pane = this.board

  }

}
