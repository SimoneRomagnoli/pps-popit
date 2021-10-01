package view

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.collections.ObservableBuffer
import javafx.scene.Node
import scalafx.scene.shape.{ Rectangle, Shape }
import utils.Constants.Screen.cellSize

import scala.language.reflectiveCalls

object Rendering {

  sealed trait RenderMode
  case object single extends RenderMode
  case object sequence extends RenderMode

  sealed trait ToBeRendered {
    def into(buffer: ObservableBuffer[Node])
    def as(renderMode: RenderMode): Seq[Shape]
  }

  case class Rendered(shapes: Seq[Shape]) extends ToBeRendered {
    override def into(buffer: ObservableBuffer[Node]): Unit = shapes foreach (buffer += _)

    override def as(renderMode: RenderMode): Seq[Shape] = renderMode match {
      case _ => shapes
    }
  }

  implicit def toSingle(shapes: Seq[Shape]): Shape = shapes.head
  implicit def renderSingle(shape: Shape): Seq[Shape] = Seq(shape)

  def a(grid: Grid): ToBeRendered = Rendered {
    grid.cells map { cell =>
      val rect: Shape = Rendering a cell
      rect.setFill(new ImagePattern(new Image("images/backgrounds/GRASS.png")))
      rect
    }
  }

  def a(entity: Entity): ToBeRendered = Rendered {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(new ImagePattern(new Image("images/" + entity.toString + ".png")))
    rectangle
  }

  def a(cell: Cell): Shape =
    Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)

  def a(track: Track): ToBeRendered = Rendered {
    track.cells
      .prepended(GridCell(-1, 0, RIGHT))
      .sliding(2)
      .map { couple =>
        val name: String =
          couple.head.direction.toString + "-" + couple.last.direction.toString + ".png"
        val cell: Cell = couple.last
        val rect: Shape = Rendering a cell
        rect.setFill(new ImagePattern(new Image("images/roads/" + name)))
        rect
      }
      .toSeq
  }

  def forInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

}
