package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderMap }
import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.RIGHT
import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane

import java.io.File

object View {

  object ViewActor {
    val canvas: Canvas = new Canvas(1200, 600)
    val board: Board = Board(Seq(canvas))

    def apply(): Behavior[Render] = Behaviors.setup { _ =>
      Behaviors.receiveMessage {
        //case RenderEntities(entities: List[Any]) => ...

        case RenderMap(grid, track) =>
          val cellSize: Int = 75
          Platform.runLater {
            val img: File = new File("res/grass-pattern.png")
            grid.cells foreach { cell =>
              canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
              canvas.graphicsContext2D.fillRect(
                cell.x * cellSize,
                cell.y * cellSize,
                cellSize,
                cellSize
              )
            }
            track.cells.prepended(GridCell(-1, 0, RIGHT)).sliding(2).foreach { couple =>
              val name: String =
                couple.head.direction.toString + "-" + couple.last.direction.toString + ".png"
              val img: File = new File("res/" + name)
              canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
              val cell: Cell = couple.last
              canvas.graphicsContext2D
                .fillRect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)

            }
          }
          Behaviors.same
        case _ => Behaviors.same
      }
    }
  }

  case class Board(var elements: Seq[Node]) extends Pane {
    children = elements

    def draw(entities: List[Any]): Unit =
      Platform.runLater {
        //entities foreach ...
      }
  }

}
