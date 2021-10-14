package model.maps

import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{ Direction, LEFT, RIGHT }
import model.maps.prolog.PrologUtils.Engines._
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.{ Solutions, Theories }
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

object Plots {

  /**
   * Trait of a plotter that decides the path on the map. It wraps a logic of plotting a track as a
   * sequence of [[Cell]] and delegates the policy of the track via template method.
   */
  trait Plotter {
    def grid: Grid
    def in(newGrid: Grid): Plotter
    def start: Direction
    def startingFrom(newStart: Direction): Plotter
    def end: Direction
    def endingAt(newEnd: Direction): Plotter
    def plot: Seq[Cell]
  }

  /**
   * Builds a [[TuPrologPlotter]] which starts a Prolog engine and finds a path into a [[Grid]].
   */
  object PrologPlotter {

    def apply(grid: Grid = gameGrid, start: Direction = LEFT, end: Direction = RIGHT): Plotter =
      TuPrologPlotter(grid, start, end)
  }

  case class TuPrologPlotter(
      override val grid: Grid,
      override val start: Direction,
      override val end: Direction,
      engine: Engine = Engine(Theories from gameGrid))
      extends Plotter {
    override def in(newGrid: Grid): Plotter = PrologPlotter(newGrid, start, end)

    override def startingFrom(newStart: Direction): Plotter = PrologPlotter(grid, newStart, end)

    override def endingAt(newEnd: Direction): Plotter = PrologPlotter(grid, start, newEnd)

    override def plot: Seq[Cell] = Solutions.trackFromPrologSolution(engine.solve(query).head)

    private def query: String =
      PrologQuery(from = grid randomInBorder start, to = grid randomInBorder end)
  }
}
