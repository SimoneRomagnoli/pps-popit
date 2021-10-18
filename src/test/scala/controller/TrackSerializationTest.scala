package controller

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import alice.tuprolog.{ SolveInfo, Struct, Term }
import controller.TrackSerializationTest.{ solve, termDecoder, termEncoder, trackFromTerm }
import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.syntax.EncoderOps
import model.Positions.Vector2DImpl
import model.entities.bullets.Bullets.Dart
import model.entities.towers.Towers.BaseTower
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{ LEFT, RIGHT }
import model.maps.prolog.PrologUtils.Engines.Engine
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.Theories
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.Scanner
import scala.collection.SeqView
import scala.language.implicitConversions

object TrackSerializationTest {
  val grid: Grid = Grid(16, 8)
  val engine: Engine = Engine(Theories from grid)
  val query: String = PrologQuery(from = grid randomInBorder LEFT, to = grid randomInBorder RIGHT)

  val solve: SeqView[SolveInfo] = engine.solve(query)

  implicit val termEncoder: Encoder[Term] = (a: Term) => {
    val string: String = a.toString
    val track: String = string
      .replace("[", "")
      .replace("]", "")
      .replaceAll("(?<!\\d),", " ")
    //.split(" ")

    println(track.split(" ").toList)

    Json.obj(("cells", Json.fromString(track)))
  }

  implicit val termDecoder: Decoder[Term] = (c: HCursor) =>
    for {
      cells <- c.downField("cells").as[String]
    } yield new String(cells)

  implicit def stringToTerm(s: String): Term = Term.createTerm(s)

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
      case (cell, i) if i == track.size - 1 => cell.direct(RIGHT)
      case (cell, i)                        => cell.directTowards(track(i + 1))
    }
  }
}

class TrackSerializationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val term: Term = solve.head.getTerm("P")
  val json: Json = term.asJson
  println(json)

  //val track: Seq[Cell] = trackFromTerm(json.as[Term].getOrElse(null))
  //println(track)
}
