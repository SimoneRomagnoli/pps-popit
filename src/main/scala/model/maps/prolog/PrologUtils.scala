package model.maps.prolog

import alice.tuprolog.{ Prolog, SolveInfo, Struct, Term, Theory }
import controller.settings.Settings.Difficulty
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.Right

import java.util.Scanner
import scala.io.Source
import scala.language.implicitConversions

object PrologUtils {

  /**
   * Contains useful operators for building a theory.
   */
  object Theories {

    val theoryResourceName: String = "theories/tracks.pl"

    def nodesToString(length: Int): String =
      LazyList
        .iterate(0)(_ + 1)
        .take(length)
        .toList
        .map(_.toString)
        .reduce((a, b) => a + "," + b)

    def theoryNodesIn(grid: Grid): String =
      "node(c(X,Y)):-member(X,[" + nodesToString(grid.width) + "]), member(Y,[" + nodesToString(
        grid.height
      ) + "]).\n"

    def gridLimits(grid: Grid): String =
      s"maxX(X):- X<${grid.width}.\n" +
        s"maxY(Y):- Y<${grid.height}.\n"

    implicit def fileToTheory[T](s: String): Theory =
      Theory.parseWithStandardOperators(s)

    /**
     * Build a Prolog theory for the specified [[Grid]].
     *
     * @param grid,
     *   the starting [[Grid]]
     * @return
     *   a [[Theory]] containing all the nodes in the grid.
     */
    def from(grid: Grid): Theory = {
      val source = Source.fromResource(theoryResourceName)
      val baseTheory: String = source.mkString
      source.close()
      gridLimits(grid) + theoryNodesIn(grid) + baseTheory
    }
  }

  /**
   * Contains constructs for building a Prolog engine.
   */
  object Engines {

    trait Engine {
      def solve: Term => LazyList[SolveInfo]
    }

    implicit def stringToTerm(s: String): Term = Term.createTerm(s)

    /**
     * A Prolog engine that solves queries.
     *
     * @param theory,
     *   the theory that the engine uses to solve queries.
     */
    case class PrologEngine(theory: Theory) extends Engine {
      val engine: Prolog = new Prolog
      engine.setTheory(theory)

      override def solve: Term => LazyList[SolveInfo] = term =>
        LazyList.continually(engine solve term)

    }

    object Engine {

      def apply(theory: Theory): Engine =
        PrologEngine(theory)
    }
  }

  /**
   * Contains elegant operators for building queries to be consumed from a Prolog engine.
   */
  object Queries {

    private def prologCell(cell: Cell): String =
      "c(" + cell.x + "," + cell.y + ")"

    private def baseQuery(from: Cell, to: Cell, difficulty: Difficulty): String =
      "path(" + prologCell(from) + ", " + prologCell(to) + ", " + difficulty.level + ", P)."

    object PrologQuery {

      def apply(from: Cell, to: Cell, difficulty: Difficulty): String =
        baseQuery(from, to, difficulty)
    }
  }

  /**
   * Contains useful methods for deserializing Prolog solutions.
   */
  object Solutions {

    def trackFromPrologSolution(prologInfo: SolveInfo): Seq[Cell] =
      trackFromTerm(prologInfo.getTerm("P"))

    def trackFromTerm(term: Term): Seq[Cell] = {
      val track: Seq[Cell] = term
        .castTo(classOf[Struct])
        .listStream()
        .map { e =>
          val scanner: Scanner = new Scanner(e.toString).useDelimiter("\\D+")
          GridCell(scanner.nextInt(), scanner.nextInt())
        }
        .toArray
        .toList
        .map(_.asInstanceOf[Cell])

      track.zipWithIndex.map {
        case (cell, i) if i == track.size - 1 => cell.direct(Right)
        case (cell, i)                        => cell.directTowards(track(i + 1))
      }
    }
  }
}
