package model.maps

import model.maps.Tracks.Directions.{DOWN, Direction, LEFT, NONE, RIGHT, UP}
import scala.language.postfixOps

object Cells {

  /**
   * Represents a cell with two integer coordinates.
   *
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
   * @param x, the x-coordinate of the cell
   * @param y, the y-coordinate of the cell
   */
  case class GridCell(override val x: Int, override val y: Int, override val direction: Direction = NONE) extends Cell {
    override def direct(dir: Direction): Cell = GridCell(x, y, dir)
  }

  /**
   * Enriched cell, with pimped operators.
   *
   * @param cell, the implicitly enriched cell.
   */
  implicit class RichCell(cell: Cell) {

    def turnRight(): Cell = cell.direct(cell.direction.turnRight)

    def turnLeft(): Cell = cell.direct(cell.direction.turnLeft)

    def nextOnTrack: Cell = cell match {
      case GridCell(x, y, dir) => dir match {
        case UP => GridCell(x, y - 1)
        case DOWN => GridCell(x, y + 1)
        case LEFT => GridCell(x - 1, y)
        case RIGHT => GridCell(x + 1, y)
        case _ => cell
      }
    }

    def directTowards(other: Cell): Cell = other match {
      case GridCell(x, _, _) if x == cell.x+1 => cell direct RIGHT
      case GridCell(x, _, _) if x == cell.x-1 => cell direct LEFT
      case GridCell(_, y, _) if y == cell.y+1 => cell direct DOWN
      case GridCell(_, y, _) if y == cell.y-1 => cell direct UP
      case _ => cell
    }
  }
}
