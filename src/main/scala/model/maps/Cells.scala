package model.maps

import model.maps.Grids.Grid
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
   * Represents a [[Cell]] in a [[Grid]].
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

    def alreadyIn(track: Seq[Cell]): Boolean = track.map(c => (c.x, c.y)).contains((cell.x, cell.y))

    def nextAlreadyIn(track: Seq[Cell]): Boolean = track.map(c => (c.x, c.y)).contains((cell.nextOnTrack.x, cell.nextOnTrack.y))

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

    def endsOutOf(grid: Grid): Boolean = cell match {
      case GridCell(x, _, _) if x >= grid.width-1 => true
      case _ => false
    }

    def pointsOutOf(grid: Grid): Boolean = cell match {
      case GridCell(x, _, LEFT) if x < 1 => true
      case GridCell(_, y, UP) if y < 1 => true
      case GridCell(_, y, DOWN) if y > grid.height-2 => true
      case _ => false
    }

    def isBorderlineOf(grid: Grid): Boolean = cell match {
      case GridCell(x, _, _) if (x == 0) || (x == grid.width-1) => true
      case GridCell(_, y, _) if (y == 0) || (y == grid.height-1) => true
      case _ => false
    }

    def isGoingOutOf(grid: Grid): Boolean = cell match {
      case GridCell(x, _, LEFT) if x < 2 => true
      case GridCell(_, y, UP) if y < 2 => true
      case GridCell(_, y, DOWN) if y > grid.height-3 => true
      case _ => false
    }

    def turnFromBorderLine(grid: Grid)(track: Seq[Cell])(last: Cell): Cell = cell nearestBorderIn grid match {
      case LEFT => if (GridCell(cell.x, cell.y, cell.direction.turnLeft) hasFreeWayFrom track) cell.turnLeft() else cell.turnRight()
      case _ => cell direct RIGHT
    }

    def turnFromBorder(grid: Grid)(track: Seq[Cell])(last: Cell): Cell = cell pointingBorderIn grid match {
      case LEFT => if (GridCell(cell.x, cell.y, cell.direction.turnLeft) hasFreeWayFrom track) cell.turnLeft() else cell.turnRight()
      case UP | DOWN => if (last.direction != LEFT) cell direct RIGHT else cell
      case _ => cell
    }

    def turnFromTrack(track: Seq[Cell]): Cell = {
      val bumpInto: (Cell, Int) = track.zipWithIndex.filter(c => c._1.x == cell.nextOnTrack.x && c._1.y == cell.nextOnTrack.y).head
      if(bumpInto._1.direction != cell.direction) {
        cell.direct(bumpInto._1.direction.opposite)
      } else {
        cell.direct(track(bumpInto._2-1).direction.opposite)
      }
    }

    def canTurnFromTrack(track: Seq[Cell])(last: Cell): Boolean = {
      val bumpInto: Cell = track.filter(c => c.x == cell.nextOnTrack.x && c.y == cell.nextOnTrack.y).head
      bumpInto.direction != cell.direction && bumpInto.direction != last.direction
    }

    def pointingBorderIn(grid: Grid): Direction = cell match {
      case GridCell(x, _, LEFT) if x < 2 => LEFT
      case GridCell(_, y, UP) if y < 2 => UP
      case GridCell(x, _, RIGHT) if x > grid.width-3 => RIGHT
      case GridCell(_, y, DOWN) if y > grid.height-3 => DOWN
      case _ => NONE
    }

    def nearestBorderIn(grid: Grid): Direction = cell match {
      case GridCell(x, _, _) if x < 2 => LEFT
      case GridCell(_, y, _) if y < 2 => UP
      case GridCell(_, y, _) if y > grid.height-3 => DOWN
      case GridCell(x, _, _) if x > grid.width-3 => RIGHT
      case _ => NONE
    }

    def hasFreeWayFrom(track: Seq[Cell]): Boolean = cell direction match {
      case UP => track.filter(_.y < cell.y).forall(_.x != cell.x)
      case DOWN => track.filter(_.y > cell.y).forall(_.x != cell.x)
      case LEFT => track.filter(_.x < cell.x).forall(_.y != cell.y)
      case RIGHT => track.filter(_.x > cell.x).forall(_.y != cell.y)
      case _ => false
    }
  }
}
