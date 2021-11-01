package model.maps

import model.Positions._
import model.Positions.Vector2D
import model.maps.Tracks.Directions.{ Direction, Down, Left, None, Right, Up }
import model.maps.Tracks.Track
import utils.Commons.Screen.cellSize

import scala.language.{ implicitConversions, postfixOps }

/**
 * This object contains the definition of a square cell of a grid.
 */
object Cells {

  /**
   * Represents a cell with two integer coordinates.
   */
  trait Cell {

    /** The x-coordinate of the cell */
    def x: Int

    /** The y-coordinate of the cell */
    def y: Int

    /** The direction of the cell */
    def direction: Direction

    /** Changes the direction of the cell */
    def direct(dir: Direction): Cell
  }

  object Cell {

    def apply(x: Int, y: Int): Cell =
      GridCell(x, y)
  }

  /**
   * Represents a [[Cell]] in a grid.
   *
   * @param x,
   *   the x-coordinate of the cell
   * @param y,
   *   the y-coordinate of the cell
   */
  case class GridCell(
      override val x: Int,
      override val y: Int,
      override val direction: Direction = None)
      extends Cell {
    override def direct(dir: Direction): Cell = GridCell(x, y, dir)
  }

  implicit def toPosition(cell: Cell): Vector2D = cell.centralPosition

  /**
   * Pimps a [[Cell]] with useful operators.
   *
   * @param cell,
   *   the pimped cell.
   */
  implicit class RichCell(cell: Cell) {

    /** Change the cell direction of 90 degrees to the right. */
    def turnRight(): Cell = cell.direct(cell.direction.turnRight)

    /** Change the cell direction of 90 degrees to the left. */
    def turnLeft(): Cell = cell.direct(cell.direction.turnLeft)

    /** Returns the next cell, depending on the direction of the current one. */
    def nextOnTrack: Cell = cell match {
      case GridCell(x, y, dir) =>
        dir match {
          case Up    => GridCell(x, y - 1)
          case Down  => GridCell(x, y + 1)
          case Left  => GridCell(x - 1, y)
          case Right => GridCell(x + 1, y)
          case _     => cell
        }
    }

    /**
     * Changes the direction of the current cell towards the specified adjoining one.
     *
     * @param other,
     *   the cell towards the current one has to be directed.
     * @return
     *   the current [[Cell]] with fixed [[Direction]].
     */
    def directTowards(other: Cell): Cell = other match {
      case GridCell(x, _, _) if x == cell.x + 1 => cell direct Right
      case GridCell(x, _, _) if x == cell.x - 1 => cell direct Left
      case GridCell(_, y, _) if y == cell.y + 1 => cell direct Down
      case GridCell(_, y, _) if y == cell.y - 1 => cell direct Up
      case _                                    => cell
    }

    /**
     * Checks whether this cell contains the specified position.
     *
     * @param position,
     *   a position in the map.
     * @return
     *   true if this cell contains the specified position.
     */
    def contains(position: Vector2D): Boolean =
      (cell.x + 1) * cellSize >= position.x &&
        cell.x * cellSize < position.x &&
        (cell.y + 1) * cellSize >= position.y &&
        cell.y * cellSize < position.y

    /**
     * Checks whether the specified track contains this cell, without considering the direction.
     *
     * @param track,
     *   a sequence of cells.
     * @return
     *   true if the specified [[Track]] contains this cell.
     */
    def in(track: Track): Boolean = track.cells.map(c => (c.x, c.y)).contains((cell.x, cell.y))

    /** Returns the top-left position of this cell on the map. */
    def topLeftPosition: Vector2D = (cell.x * cellSize, cell.y * cellSize)

    /** Returns the position of the center of this cell on the map. */
    def centralPosition: Vector2D = this.topLeftPosition + (cellSize / 2, cellSize / 2)

    /**
     * Gives a position in the cell that depends on the previous direction if the percentage of
     * crossed cell is lower than half cell, or on the current one otherwise.
     *
     * @param percentage,
     *   the percentage of crossed cell.
     * @param previous,
     *   the direction of the previous cell.
     * @return
     *   the exact position on the cell depending on the crossed percentage.
     */
    def positionFromCrossed(percentage: Double)(implicit previous: Direction): Vector2D =
      percentage match {
        case x if x < 0.5 => crossingPosition(previous)(percentage)
        case _            => crossingPosition(cell direction)(percentage)
      }

    /**
     * Given a direction and a percentage of crossed cell, it calculates the exact position in this
     * cell depending on the percentage of crossed cell and the crossing direction.
     *
     * @param direction,
     *   the crossing direction.
     * @param percentage,
     *   the percentage of crossed cell.
     * @return
     *   the exact position on the cell depending on the crossed percentage.
     */
    private def crossingPosition(direction: Direction)(percentage: Double): Vector2D =
      direction match {
        case Up    => cell.topLeftPosition + (cellSize / 2, cellSize * (1 - percentage))
        case Down  => cell.topLeftPosition + (cellSize / 2, cellSize * percentage)
        case Left  => cell.topLeftPosition + (cellSize * (1 - percentage), cellSize / 2)
        case Right => cell.topLeftPosition + (cellSize * percentage, cellSize / 2)
        case _     => cell.topLeftPosition + (cellSize / 2, cellSize / 2)
      }
  }
}
