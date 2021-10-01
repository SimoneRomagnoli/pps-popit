package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.scene.shape.{ Rectangle, Shape }
import utils.Constants.Screen.cellSize
import view.render.Drawing.{ Grass, Item, Road }
import view.render.Renders.{ renderSingle, Rendered, ToBeRendered }

import scala.language.implicitConversions

/**
 * Object that simulates a DSL for rendering logic entities as shapes for a scalafx pane.
 */
object Rendering {

  /** Renders a [[Grid]] with grass drawings. */
  def a(grid: Grid): ToBeRendered = Rendered {
    grid.cells map { cell =>
      val rect: Shape = Rendering a cell
      rect.setFill(Drawing the Grass)
      rect
    }
  }

  /** Renders an [[Entity]] with its corresponding drawing. */
  def a(entity: Entity): ToBeRendered = Rendered {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(Drawing the Item(entity))
    rectangle
  }

  /** Renders a [[Cell]] just with a square [[Shape]]. */
  def a(cell: Cell): Shape =
    Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)

  /** Renders a [[Track]] as a sequence of road drawings. */
  def a(track: Track): ToBeRendered = Rendered {
    track.cells
      .prepended(GridCell(-1, 0, RIGHT))
      .sliding(2)
      .map { couple =>
        val dir: String = couple.head.direction.toString + "-" + couple.last.direction.toString
        val cell: Cell = couple.last
        val rect: Shape = Rendering a cell
        rect.setFill(Drawing the Road(dir))
        rect
      }
      .toSeq
  }

  /** Utility method for generating a [[Rectangle]] with the specified picture path. */
  def forInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

}
