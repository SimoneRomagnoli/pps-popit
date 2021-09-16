package model.maps

import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Plots.{PushDownAutomatonPlotter, TrackPlotter}

object Tracks {

  /**
   * Represents the possible directions of a [[Cell]].
   *
   */
  object Directions {
    sealed trait Direction {
      def opposite: Direction = this match {
        case UP => DOWN
        case DOWN => UP
        case LEFT => RIGHT
        case RIGHT => LEFT
        case _ => NONE
      }

      def turnLeft: Direction = this match {
        case UP => LEFT
        case DOWN => RIGHT
        case LEFT => DOWN
        case RIGHT => UP
        case _ => NONE
      }

      def turnRight: Direction = this.turnLeft.opposite
    }
    case object NONE extends Direction
    case object UP extends Direction
    case object DOWN extends Direction
    case object LEFT extends Direction
    case object RIGHT extends Direction

    /**
     * Given the ordered first and second direction, it determines if they form a left or right turn
     *
     * @param first [[Direction]]
     * @param second [[Direction]]
     * @return [[LEFT]], [[RIGHT]] or [[NONE]]
     */
    def turnBetween(first: Direction, second: Direction): Direction = first match {
      case UP if second == RIGHT || second == LEFT => second
      case DOWN if second == RIGHT || second == LEFT => second.opposite
      case LEFT if second == UP || second == DOWN => second.turnRight
      case RIGHT if second == UP || second == DOWN => second.turnLeft
      case _ => NONE
    }
  }

  /**
   * Represents a path in a [[Grid]].
   *
   */
  trait Track {

    /** Returns the cells in the [[Track]]. */
    def cells: Seq[Cell]
  }

  case class TrackMap(override val cells: Seq[Cell]) extends Track

  object Track {

    def apply(grid: Grid)(implicit trackPlotter: TrackPlotter = PushDownAutomatonPlotter.pdaPlotter): Track =
      TrackMap(trackPlotter plot grid)
  }
}
