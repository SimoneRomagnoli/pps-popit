package model.maps.prolog

import alice.tuprolog.{Prolog, SolveInfo, Struct, Term, Theory}
import model.maps.Cells.{Cell, GridCell}
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT

import java.util.Scanner
import scala.collection.SeqView
import scala.io.Source

object PrologUtils {
  object Theories {

    val theoryResourceName: String = "res/theory.pl"

    def nodesToString(length: Int): String =
      LazyList.iterate(0)(_+1).take(length)
        .toList
        .toString()
        .replace("List(", "")
        .replace(")","")

    def theoryNodesIn(grid: Grid): String =
      "node(c(X,Y)):-member(X,["+nodesToString(grid.width)+"]), member(Y,["+nodesToString(grid.height)+"]).\n"

    implicit def fileToTheory[T](s: String): Theory =
      Theory.parseWithStandardOperators(s)

    def from(grid: Grid): Theory = {
      val source = Source.fromFile(theoryResourceName)
      val baseTheory: String = source.mkString
      source.close()
      theoryNodesIn(grid) + baseTheory
    }
  }

  object Engines {

    trait Engine {
      def solve: Term => SeqView[SolveInfo]
    }

    implicit def stringToTerm(s: String): Term = Term.createTerm(s)
    implicit def seqToTerm[T](s: Seq[T]): Term = s.mkString("[",",","]")

    case class PrologEngine(theory: Theory) extends Engine {
      val engine: Prolog = new Prolog
      engine.setTheory(theory)

      override def solve: Term => SeqView[SolveInfo] = term =>
        new SeqView[SolveInfo] {
          override def apply(i: Int): SolveInfo = {
            for(_ <- 0 until i if iterator.hasNext) iterator.next()
            iterator.next()
          }

          override def length: Int = {
            var count: Int = 0
            while(iterator.hasNext) {
              count += 1
              iterator.next()
            }
            count
          }

          override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo] {
            var solution: Option[SolveInfo] = Some(engine.solve(term))

            override def hasNext: Boolean = solution.isDefined &&
              (solution.get.isSuccess || solution.get.hasOpenAlternatives)

            override def next(): SolveInfo = {
              try solution.get
              finally solution = if (solution.get.hasOpenAlternatives) Some(engine.solveNext()) else None
            }
          }
        }

    }

    object Engine {
      def apply(theory: Theory): Engine = {
        PrologEngine(theory)
      }
    }
  }

  object Queries {

    def prologCell(cell: Cell): String =
      cell.toString.replace("GridCell", "c").replace(",NONE", "")

    def baseQuery(from: Cell, to: Cell): String =
      "allPath("+prologCell(from)+", "+prologCell(to)+", P)."

    object PrologQuery {
      def apply(from: Cell, to: Cell): String = {
        baseQuery(from, to)
      }
    }
  }

  object Solutions {

    def trackFromPrologSolution(prologInfo: SolveInfo): Seq[Cell] = {
      val directionlessTrack: List[Cell] = prologInfo.getTerm("P")
        .castTo(classOf[Struct])
        .listStream()
        .map(e => {
          val scanner: Scanner = new Scanner(e.toString).useDelimiter("\\D+")
          GridCell(scanner.nextInt(), scanner.nextInt())
        })
        .toArray.toList.map(_.asInstanceOf[Cell])

      var track: Seq[Cell] = Seq()
      for(i <- 0 until directionlessTrack.size-1) track = track :+ directionlessTrack(i).directTowards(directionlessTrack(i+1))
      track = track :+ directionlessTrack.last.direct(RIGHT)
      track
    }
  }
}
