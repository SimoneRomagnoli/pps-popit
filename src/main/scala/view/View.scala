package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ Render, RenderEntities, RenderLoading, RenderMap }
import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.maps.Cells.{ cellSize, Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.application.Platform
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.Pane
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Font
import utils.Constants

import java.io.{ File, FileInputStream }
import scala.io.Source
import scala.util.Random

object View {

  object ViewActor {
    val board: Board = Board(Seq())

    def apply(): Behavior[Render] = Behaviors.setup { _ =>
      Behaviors.receiveMessage {
        case RenderLoading() =>
          board.loading()
          Behaviors.same

        case RenderEntities(entities: List[Entity]) =>
          board draw entities
          Behaviors.same

        case RenderMap(track) =>
          board draw track
          Behaviors.same

        case _ => Behaviors.same
      }
    }
  }

  case class Board(var elements: Seq[Node]) extends Pane {
    children = elements
    var mapNodes: Int = 0
    val grid: Grid = Grid(Constants.widthRatio, Constants.heightRatio)
    val grass: File = new File("src/main/resources/images/backgrounds/GRASS.png")

    grid.cells foreach { cell =>
      val rect: Rectangle = Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
      rect.setFill(new ImagePattern(new Image(grass.toURI.toString)))
      children.add(rect)
    }

    def loading(): Unit = Platform.runLater {
      val loadingLabel: Label =
        Label(Constants.loadingLabels(Random.between(0, Constants.loadingLabels.size)))
      loadingLabel.setStyle("-fx-font-weight:bold; -fx-font-size:25px;")
      loadingLabel
        .layoutXProperty()
        .bind(this.widthProperty().subtract(loadingLabel.widthProperty()).divide(2))
      loadingLabel
        .layoutYProperty()
        .bind(this.heightProperty().subtract(loadingLabel.heightProperty()).divide(2))

      children.add(loadingLabel)
    }

    def draw(track: Track): Unit = Platform.runLater {
      children.remove(children.size - 1)
      mapNodes = grid.width * grid.height + track.cells.size
      val img: File = new File("src/main/resources/images/backgrounds/GRASS.png")
      grid.cells foreach { cell =>
        val rect: Rectangle = Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
        rect.setFill(new ImagePattern(new Image(img.toURI.toString)))
        children.add(rect)
      }
      track.cells.prepended(GridCell(-1, 0, RIGHT)).sliding(2).foreach { couple =>
        val name: String =
          couple.head.direction.toString + "-" + couple.last.direction.toString + ".png"
        val img: File = new File("src/main/resources/images/roads/" + name)
        val cell: Cell = couple.last
        val rect: Rectangle = Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
        rect.setFill(new ImagePattern(new Image(img.toURI.toString)))
        children.add(rect)
      }
    }

    def draw(entities: List[Any]): Unit = Platform.runLater {
      children.removeRange(mapNodes, children.size)
      entities foreach {
        case balloon: Balloon =>
          val img: File = new File("src/main/resources/images/balloons/RED.png")
          val rect: Rectangle = Rectangle(
            balloon.position.x,
            balloon.position.y,
            balloon.boundary._1,
            balloon.boundary._2
          )
          rect.setFill(new ImagePattern(new Image(img.toURI.toString)))
          children.add(rect)
        case _ =>
      }
    }
  }

}
