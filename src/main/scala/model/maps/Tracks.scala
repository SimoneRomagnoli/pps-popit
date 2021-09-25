package model.maps

import model.Positions.Vector2D
import utils.Constants
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Plots.{ Plotter, PrologPlotter }
import model.maps.Tracks.Directions.{ LEFT, RIGHT }

import scala.language.postfixOps

object Tracks {

  /**
   * Represents the possible directions of a [[Cell]].
   */
  object Directions {

    sealed trait Direction {

      def opposite: Direction = this match {
        case UP    => DOWN
        case DOWN  => UP
        case LEFT  => RIGHT
        case RIGHT => LEFT
        case _     => NONE
      }

      def turnLeft: Direction = this match {
        case UP    => LEFT
        case DOWN  => RIGHT
        case LEFT  => DOWN
        case RIGHT => UP
        case _     => NONE
      }

      def turnRight: Direction = this.turnLeft.opposite
    }
    case object NONE extends Direction
    case object UP extends Direction
    case object DOWN extends Direction
    case object LEFT extends Direction
    case object RIGHT extends Direction
  }

  /**
   * Represents a path in a [[Grid]].
   */
  trait Track {

    /** Returns the cells in the [[Track]]. */
    def cells: Seq[Cell]

    def start: Cell = cells.head
    def finish: Cell = cells.last
  }

  case class TrackMap(override val cells: Seq[Cell]) extends Track

  object Track {

    def apply(): Track =
      TrackMap(Constants.Maps.basicTrack)

    def apply(grid: Grid, plotter: Plotter = PrologPlotter()): Track =
      TrackMap(plotter in grid startingFrom LEFT endingAt RIGHT plot)
  }

  implicit class RichTrack(track: Track) {

    def exactPositionFrom(linearPosition: Double): Vector2D =
      track
        .cells(linearPosition.toInt)
        .vectorialPosition(linearPosition.toInt match {
          case 0 => RIGHT
          case _ => track.cells(linearPosition.toInt - 1).direction
        })(linearPosition - linearPosition.toInt)
  }
}
