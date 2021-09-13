package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.Render
import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane

object View {

  object ViewActor {
    val canvas: Canvas = new Canvas(800, 600)
    val board: Board = Board(Seq(canvas))

    def apply(): Behavior[Render] = Behaviors.setup { ctx =>

      Behaviors.receiveMessage {
        //case RenderEntities(entities: List[Any]) => ...
        case _ => Behaviors.same
      }
    }
  }

  case class Board(var elements: Seq[Node]) extends Pane {
    children = elements

    def draw(entities: List[Any]): Unit = {
      Platform.runLater {
        //entities foreach ...
      }
    }
  }

}
