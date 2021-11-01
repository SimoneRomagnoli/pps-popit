package model.maps

import model.Positions.Vector2D
import model.maps.Cells.Cell
import model.maps.Tracks.Directions.{ Direction, Right }
import utils.Commons

import scala.language.postfixOps

/**
 * This object contains the definitions of the game tracks, defined as sequences of [[Cell]] s.
 */
object Tracks {

  /**
   * Contains the possible directions of a [[Cell]].
   */
  object Directions {

    /** A [[Cell]] can have one of the four directions in the two dimensions. */
    sealed trait Direction {

      def opposite: Direction = this match {
        case Up    => Down
        case Down  => Up
        case Left  => Right
        case Right => Left
        case _     => None
      }

      def turnLeft: Direction = this match {
        case Up    => Left
        case Down  => Right
        case Left  => Down
        case Right => Up
        case _     => None
      }

      def turnRight: Direction = this.turnLeft.opposite
    }

    case object None extends Direction
    case object Up extends Direction
    case object Down extends Direction
    case object Left extends Direction
    case object Right extends Direction
  }

  /**
   * Represents a path in a grid, defined as a sequence of [[Cell]] s.
   */
  trait Track {

    /** Returns the ordered cells in the [[Track]]. */
    def cells: Seq[Cell]

    /** Returns the first [[Cell]] of this [[Track]]. */
    def start: Cell = cells.head

    /** Returns the last [[Cell]] of this [[Track]]. */
    def finish: Cell = cells.last
  }

  case class TrackMap(override val cells: Seq[Cell]) extends Track

  object Track {

    def apply(cells: Seq[Cell] = Commons.Maps.basicTrack): Track =
      TrackMap(cells)
  }

  /**
   * Pimps a [[Track]] with useful methods related to linear positions in the grid-dependent track.
   *
   * @param track,
   *   the pimped track.
   */
  implicit class RichTrack(track: Track) {

    /**
     * Given a linear position, it returns the direction of the cell relative to the linear
     * position.
     *
     * @param linearPosition,
     *   double representing the position in the track.
     * @return
     *   the [[Direction]] of the [[Cell]] in the specified linear position.
     */
    def directionIn(linearPosition: Double): Direction =
      track.cells(linearPosition.toInt).direction

    /**
     * Given a linear position, it returns the exact position in the game grid relative to the
     * linear position in the track. The linear position is a double, in which the integer part of
     * it represents the index of the cell in the sequence of the track, while the decimal part of
     * it represents the passed percentage of the current cell depending on its direction.
     *
     * @param linearPosition,
     *   double representing the position in the [[Track]].
     * @return
     *   the exact position as a [[Vector2D]] in the entire game grid.
     */
    def exactPositionFrom(linearPosition: Double): Vector2D = linearPosition.toInt match {
      case outOfBounds if outOfBounds >= track.cells.size =>
        track.cells.last.nextOnTrack.centralPosition
      case intPosition =>
        track
          .cells(intPosition)
          .vectorialPosition(intPosition match {
            case 0                                              => Right
            case outOfBounds if outOfBounds >= track.cells.size => Right
            case _ => track.cells(intPosition - 1).direction
          })(linearPosition - intPosition)
    }
  }
}
