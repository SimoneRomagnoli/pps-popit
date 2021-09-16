package model.maps

import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{Direction, LEFT, NONE, RIGHT, turnBetween}
import model.maps.Tracks.Plots.{TrackPlotter, randomTrackPlotter}

import scala.annotation.tailrec
import scala.collection.mutable.{Stack => MutableStack}

object Tracks {

  /**
   * Represents the possible directions of a cell.
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

    def turnBetween(first: Direction, second: Direction): Direction = first match {
      case UP if second == RIGHT || second == LEFT => second
      case DOWN if second == RIGHT || second == LEFT => second.opposite
      case LEFT if second == UP || second == DOWN => second.turnRight
      case RIGHT if second == UP || second == DOWN => second.turnLeft
      case _ => NONE
    }
  }

  /**
   * Represents a path in a grid.
   *
   */
  trait Track {

    /** Returns the cells in the track. */
    def cells: Seq[Cell]
  }

  case class TrackMap(override val cells: Seq[Cell]) extends Track

  object Track {

    def apply(grid: Grid)(implicit trackPlotter: TrackPlotter = randomTrackPlotter): Track =
      TrackMap(trackPlotter plot grid)
  }

  object Plots {

    /**
     * Trait of a plotter that decides the path on the map.
     * It wraps a logic of plotting a track as a sequence of cells
     * and delegates the policy of the track via template method.
     *
     */
    trait TrackPlotter {

      /**
       * Plots a track on a grid.
       *
       * @param grid, the grid on which plot the track
       * @return the plotted track
       */
      def plot(grid: Grid): Seq[Cell] = {
        @tailrec
        def _plot(track: Seq[Cell], last: Cell): Seq[Cell] = next(track)(last) match {
          case cell if cell pointsOutOf grid => if (track.size > grid.width) track :+ last :+ cell else _plot(track :+ last, cell.turnFromBorder(grid)(track)(last))
          case cell if cell isGoingOutOf grid => _plot(track :+ last, cell.turnFromBorder(grid)(track)(last))
          case cell if track.map(c => (c.x, c.y)).contains((cell.nextOnTrack.x, cell.nextOnTrack.y)) => _plot(track :+ last, cell turnFromTrackAheadOfOne track)
          case cell if track.map(c => (c.x, c.y)).contains((cell.nextOnTrack.direct(cell.direction).nextOnTrack.x, cell.nextOnTrack.direct(cell.direction).nextOnTrack.y)) => _plot(track :+ last, cell turnFromTrackAheadOfTwo track)
          case cell => _plot(track :+ last, cell)
        }

        _plot(Seq(), grid startFrom LEFT direct RIGHT)
      }

      /**
       * Defines the next cell of the track depending on the previous one.
       * It cannot be the same as the previous one.
       *
       * @param track, the defined track
       * @param last, the last cell to be added in the track
       * @return the next cell of the track.
       */
      def next(track: Seq[Cell])(last: Cell): Cell = last.nextOnTrack.direct(nextDirection(track, last))

      /**
       * Determines the next direction on the map depending on the previous one.
       * The implementation of this function is delegated via template method.
       *
       * @param track, the defined track
       * @return the next direction
       */
      def nextDirection(track: Seq[Cell], last: Cell): Direction
    }

    val randomTrackPlotter: TrackPlotter = (track: Seq[Cell], last: Cell) => {
      val prev: Direction = last.direction
      val directionStack: MutableStack[Direction] = directionsStack(track)
      println(directionStack)
      val sameDirectionCounter: Int = if(track isEmpty) 1 else track.size - track.zipWithIndex.reverse.find(_._1.direction != prev).getOrElse((track.last, 0))._2
      if (Math.random() < 1 - 0.1 * sameDirectionCounter) prev
      else if (directionStack.size >= 2) {
        directionStack.head match {
          case LEFT => prev.turnRight
          case RIGHT => prev.turnLeft
          case _ => prev
        }
      } else if (Math.random() < 0.5) prev.turnRight else prev.turnLeft

    }

    def directionsStack(track: Seq[Cell]): MutableStack[Direction] = {
      @tailrec
      def _directionsStack(stack: MutableStack[Direction], dirs: Seq[Direction]): MutableStack[Direction] = dirs match {
        case h +: t => if(t.nonEmpty && turnBetween(h, t.head) != NONE) directionPDA(stack, turnBetween(h, t.head)); _directionsStack(stack, t)
        //case Seq(h) => if(turnBetween(stack.head, h) != NONE) directionPDA(stack, turnBetween(stack.head, h)); stack
        case Seq() => stack
      }

      _directionsStack(MutableStack[Direction](), track.map(_.direction))
    }

    def directionPDA(stack: MutableStack[Direction], nextDirection: Direction): Unit = {
      if (stack.isEmpty || stack.head == nextDirection) {
        stack.push(nextDirection)
      } else {
        stack.pop()
      }
    }
  }

}
