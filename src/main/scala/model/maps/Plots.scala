package model.maps

import alice.tuprolog.SolveInfo
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{Direction, LEFT, RIGHT}
import model.maps.prolog.PrologUtils.Engines._
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.{Solutions, Theories}

import scala.collection.SeqView
import scala.language.postfixOps

object Plots {

  /**
   * Trait of a plotter that decides the path on the map.
   * It wraps a logic of plotting a track as a sequence of [[Cell]]
   * and delegates the policy of the track via template method.
   *
   */
  trait Plotter {
    def grid: Grid
    def in(g: Grid): Plotter
    def start: Direction
    def startingFrom(direction: Direction): Plotter
    def end: Direction
    def endingAt(direction: Direction): Plotter
    def plot: Seq[Cell]
  }

  case class TuPrologPlotter(override val grid: Grid, override val start: Direction, override val end: Direction) extends Plotter {
    override def in(set: Grid): Plotter = PrologPlotter(grid = set)

    override def startingFrom(direction: Direction): Plotter = PrologPlotter(start = direction)

    override def endingAt(direction: Direction): Plotter = PrologPlotter(end = direction)

    override def plot: Seq[Cell] = {
      val solutionStream: SeqView[SolveInfo] = Engine(Theories from grid).solve(PrologQuery(from = grid randomInBorder start, to = grid randomInBorder end))
      Solutions.trackFromPrologSolution(solutionStream.head)
    }
  }

  object PrologPlotter {

    def apply(grid: Grid = Grid(16, 8), start: Direction = LEFT, end: Direction = RIGHT): Plotter =
      TuPrologPlotter(grid, start, end)
  }
}