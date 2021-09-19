package model.maps

import alice.tuprolog.{Prolog, SolveInfo, Struct, Term, Theory}
import model.maps.Cells.{Cell, GridCell}
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track

import java.io.{File, FileInputStream}
import java.util.Scanner
import scala.collection.SeqView

object Scala2P {
  def extractTerm(solveInfo:SolveInfo, i:Integer): Term =
    solveInfo.getSolution.asInstanceOf[Struct].getArg(i).getTerm

  def extractTerm(solveInfo:SolveInfo, s:String): Term =
    solveInfo.getTerm(s)

  implicit def stringToTerm(s: String): Term = Term.createTerm(s)
  implicit def seqToTerm[T](s: Seq[T]): Term = s.mkString("[",",","]")
  implicit def fileToTheory[T](s: String): Theory = Theory.parseWithStandardOperators(new FileInputStream(new File(s)))

  def mkPrologEngine(theory: Theory): Term => SeqView[SolveInfo] = {
    val engine = new Prolog
    engine.setTheory(theory)
    goal => new SeqView[SolveInfo]{
      override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo] {
        var solution: Option[SolveInfo] = Some(engine.solve(goal))

        override def hasNext: Boolean = solution.isDefined &&
        (solution.get.isSuccess || solution.get.hasOpenAlternatives)

        override def next(): SolveInfo = {
          try solution.get
          finally solution = if (solution.get.hasOpenAlternatives) Some(engine.solveNext()) else None
        }
      }

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
    }
  }

  def solveWithSuccess(engine: Term => List[SolveInfo], goal: Term): Boolean =
    engine(goal).map(_.isSuccess).headOption.contains(true)

  def solveOneAndGetTerm(engine: Term => List[SolveInfo], goal: Term, term: String): Term =
    engine(goal).headOption.map(extractTerm(_,term)).get

  def getTrack(prologInfo: SolveInfo): Track = {
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
    Track(track)
  }
}
