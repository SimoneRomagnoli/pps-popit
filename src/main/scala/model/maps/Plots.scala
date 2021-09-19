package model.maps

import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{Direction, LEFT, NONE, RIGHT, turnBetween}

import scala.collection.mutable.{Stack => MutableStack}
import scala.annotation.tailrec
import scala.language.postfixOps

object Plots {

  /**
   * Trait of a plotter that decides the path on the map.
   * It wraps a logic of plotting a track as a sequence of [[Cell]]
   * and delegates the policy of the track via template method.
   *
   */
  trait TrackPlotter {

    /**
     * Plots a track on a [[Grid]].
     *
     * @param grid, the [[Grid]] on which plot the path
     * @return the plotted track
     */
    def plot(grid: Grid): Seq[Cell] = {
      @tailrec
      def _plot(track: Seq[Cell], last: Cell): Seq[Cell] = next(track)(last) match {
        case cell if cell endsOutOf grid => println("end");track :+ last :+ cell.direct(RIGHT)
        //case cell if cell isGoingOutOf grid => println("OUT: "+cell+" -> "+cell.turnFromBorder(grid)(track)(last)); _plot(track :+ last, cell.turnFromBorder(grid)(track)(last))
        case cell if cell pointsOutOf grid => println("OUT: "+cell+" -> "+cell.turnFromBorder(grid)(track)(last)); _plot(track, last.turnFromBorder(grid)(track)(track.last))
        case cell if cell nextAlreadyIn track => println("TRACK: "+cell+" -> "+cell.turnFromTrack(track)); _plot(track :+ last, cell turnFromTrack track)
        //case cell if cell isBorderlineOf grid => println("border");_plot(track :+ last, cell.turnFromBorderLine(grid)(track)(last))
        case cell => println("normal");_plot(track :+ last, cell)
      }

      _plot(Seq(), grid startFrom LEFT direct RIGHT)
    }

    /**
     * Defines the next [[Cell]] of the track depending on the previous one.
     *
     * @param track, the defined track
     * @param last, the last [[Cell]] to be added in the path
     * @return the next [[Cell]] of the track.
     */
    def next(track: Seq[Cell])(last: Cell): Cell = last.nextOnTrack.direct(nextDirection(track, last))

    /**
     * Determines the next [[Direction]] on the map depending on the previous ones.
     * The implementation of this function is delegated via template method.
     *
     * @param track, the defined track
     * @return the next [[Direction]]
     */
    def nextDirection(track: Seq[Cell], last: Cell): Direction
  }

  /**
   * Defines the plot of a track with a push-down automaton:
   * the plot can turn in the same direction only 2 times,
   * in order to avoid repeating cells.
   * The stack is pushed up with similar [[Direction]] changes
   * and is popped when a different [[Direction]] changes results.
   *
   */
  object PushDownAutomatonPlotter {
    val stackMaximumSize: Int = 2

    val pdaPlotter: TrackPlotter = (track: Seq[Cell], last: Cell) => Math.random() match {
      case forward if forward < 1 - 0.3 * cellsWithoutTurning(track)(last) => last direction
      case _ => directionsStack(track) match {
        case fullStack if fullStack.size >= stackMaximumSize => fullStack.head match {
          case LEFT => println("pda force turn right");last.direction.turnRight
          case RIGHT => println("pda force turn left");last.direction.turnLeft
          case _ => last.direction
        }
        case _ if Math.random() < 0.5 => last.direction.turnRight
        case _ => last.direction.turnLeft
      }

    }

    def lastTwoDirectionChanges(track: Seq[Cell])(last: Cell): Seq[Direction] = {
      @tailrec
      def _directions(dirs: Seq[Direction], acc: Seq[Direction]): Seq[Direction] = dirs match {
        case h +: t => if (t.nonEmpty && turnBetween(h, t.head) != NONE) acc :+ turnBetween(h, t.head); _directions(t, acc)
        case Seq() => if(turnBetween(track.last.direction, last.direction) != NONE) acc :+ turnBetween(track.last.direction, last.direction) else acc
      }

      _directions(track.map(_.direction), Seq()).takeRight(2)
    }

    /**
     * Determines how many cells have the same [[Direction]] in the last part of the plotted track
     *
     * @param track, the track
     * @param last, the last [[Cell]]
     * @return the number of consecutive straight cells
     */
    def cellsWithoutTurning(track: Seq[Cell])(last: Cell): Int = track match {
      case Seq() => 0
      case _ => track.size - track.zipWithIndex.reverse.find(_._1.direction != last.direction).getOrElse((track.last, 0))._2
    }

    /**
     * Determines the number of excessive turn to left or to the right in the sequence of [[Direction]]
     *
     * @param track, the plotted track
     * @return the stack of excessive direction changes
     */
    def directionsStack(track: Seq[Cell]): MutableStack[Direction] = {
      @tailrec
      def _directionsStack(stack: MutableStack[Direction], dirs: Seq[Direction]): MutableStack[Direction] = dirs match {
        case h +: t => if (t.nonEmpty && turnBetween(h, t.head) != NONE) pushOrPop(stack, turnBetween(h, t.head)); _directionsStack(stack, t)
        case Seq() => stack
      }

      _directionsStack(MutableStack[Direction](), track.map(_.direction))
    }

    /**
     * Pop the stack if the direction is opposite to the last one,
     * push it otherwise.
     *
     * @param stack, the current stack of [[Direction]] changes
     * @param nextDirection, the current [[Direction]]
     */
    def pushOrPop(stack: MutableStack[Direction], nextDirection: Direction): Unit = {
      if (stack.isEmpty || stack.head == nextDirection) {
        stack.push(nextDirection)
      } else {
        stack.clear()
        //stack.pop()
      }
    }
  }
}