package view

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.scene.shape.{ Rectangle, Shape }
import utils.Constants.Screen.cellSize

object Rendering {

  def a(grid: Grid): Seq[Shape] =
    grid.cells map { cell =>
      val rect: Shape = Rendering a cell
      rect.setFill(new ImagePattern(new Image("images/backgrounds/GRASS.png")))
      rect
    }

  def a(entity: Entity): Shape = {
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

  def a(track: Track): Seq[Shape] =
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

  def forInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

}
