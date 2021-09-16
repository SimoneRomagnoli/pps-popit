package model.maps

/**
 * Represents a cell in the grid of the map.
 *
 * @param x, the x-coordinate of the cell
 * @param y, the y-coordinate of the cell
 */
case class Cell(x: Int, y: Int)

/**
 * Represents the logical structure of the game beneath the map.
 *
 */
trait Grid {

  /** Defines the number of cells on the x-axis */
  def width: Int

  /** Defines the number of cells on the y-axis */
  def height: Int

  /** Returns the sequence of all the cells in the grid. */
  def cells: Seq[Cell]
}

object Grid {

  def apply(width: Int, height: Int): Grid = {

  }
}

case class GridMap(override val width: Int, override val height: Int) extends Grid {
  /** Returns the sequence of all the cells in the grid. */
  override def cells: Seq[Cell] = ???
}
