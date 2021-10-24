package controller

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import alice.tuprolog.Term
import controller.TrackSerializationTest.{ engine, listSize, query }
import controller.files.FileCoders.{ trackDecoder, trackEncoder }
import io.circe.Json
import io.circe.syntax.EncoderOps
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{ LEFT, RIGHT }
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Engines.Engine
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm
import model.maps.prolog.PrologUtils.Theories
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.{ implicitConversions, postfixOps }

object TrackSerializationTest {
  val grid: Grid = Grid(16, 8)
  val engine: Engine = Engine(Theories from grid)
  val query: String = PrologQuery(from = grid randomInBorder LEFT, to = grid randomInBorder RIGHT)

  val listSize: Int = 5

  /*implicit val termEncoder: Encoder[Term] = (a: Term) => {
    val track: List[String] = a.toString
      .replace("[", "")
      .replace("]", "")
      .replaceAll("(?<!\\d),", " ")
      .split(" ")
      .toList

    Json.obj(("cells", Json.fromValues(track.map(s => Json.fromString(s)))))
  }

  implicit val termDecoder: Decoder[Term] = (c: HCursor) =>
    for {
      track <- c.downField("cells").as[List[String]]
    } yield Term.createTerm(track.mkString("[", ", ", "]"))*/

}

class TrackSerializationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "The controller" when {
    "it has to save a list of tracks on file" should {
      "serialize and deserialize the entire list to save and retrieve it" in {
        val trackList: List[Track] =
          List
            .range(1, listSize + 1)
            .flatMap(_ =>
              List(Track(trackFromTerm(engine.solve(Term.createTerm(query)).head.getTerm("P"))))
            )

        val serialized: Json = trackList.asJson

        val deserialized: List[Track] = serialized.as[List[Track]].getOrElse(List())

        for (i <- trackList.indices)
          trackList(i).equals(deserialized(i)) shouldBe true
      }
    }
  }

}
