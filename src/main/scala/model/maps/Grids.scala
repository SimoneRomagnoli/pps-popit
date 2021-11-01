package model.maps

import model.Positions.Vector2D
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.{ Direction, Down, Left, Right, Up }
import utils.Commons.Screen.cellSize

import scala.language.postfixOps
import scala.util.Random

/**
 * This objects contains the definition of a grid beneath the game.
 */
object Grids {

  /**
   * Represents the logical structure of the map beneath the game.
   */
  trait Grid {

    /** Defines the number of cells on the x-axis */
    def width: Int

    /** Defines the number of cells on the y-axis */
    def height: Int

    /** Returns the sequence of all the cells in the grid. */
    def cells: Seq[Cell]
  }

  /**
   * Represents a normal grid formed of square cells.
   *
   * @param width,
   *   the number of cells on the x-axis
   * @param height,
   *   the number of cells on the y-axis
   */
  case class GridMap(override val width: Int, override val height: Int) extends Grid {

    override def cells: Seq[Cell] = for {
      x <- 0 until width
      y <- 0 until height
    } yield Cell(x, y)
  }

  object Grid {

    def apply(width: Int, height: Int): Grid =
      GridMap(width, height)
  }

  /**
   * Pimps a [[Grid]] with useful methods.
   *
   * @param grid,
   *   the pimped grid.
   */
  implicit class RichGrid(grid: Grid) {

    /**
     * Returns a border of the grid as a sequence of cells.
     *
     * @param direction,
     *   one of the four directions.
     * @return
     *   the sequence of [[Cell]] s on the border of the specified [[Direction]].
     */
    def border(direction: Direction): Seq[Cell] = direction match {
      case Left  => grid.cells.filter(_.x == 0)
      case Up    => grid.cells.filter(_.y == 0)
      case Right => grid.cells.filter(_.x == grid.width - 1)
      case Down  => grid.cells.filter(_.y == grid.height - 1)
      case _     => grid cells
    }

    /**
     * Returns a random cell on the border of the specified direction.
     *
     * @param direction,
     *   one of the four directions.
     * @return
     *   a random [[Cell]] on the specified border.
     */
    def randomInBorder(direction: Direction): Cell =
      (Random shuffle grid.border(direction)) head

    /**
     * Returns a cell containing the specified position.
     *
     * @param position,
     *   the exact position of the game as a [[Vector2D]].
     * @return
     *   the [[Cell]] containing the specified position.
     */
    def specificCell(position: Vector2D): Cell =
      GridCell((position.x / cellSize).toInt, (position.y / cellSize).toInt)
  }

}
