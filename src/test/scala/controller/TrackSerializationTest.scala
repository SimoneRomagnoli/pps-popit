package controller

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import alice.tuprolog.Term
import controller.TrackSerializationTest.{ engine, listSize, query }
import controller.files.FileCoder
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{ LEFT, RIGHT }
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Engines.Engine
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm
import model.maps.prolog.PrologUtils.Theories
import org.scalatest.wordspec.AnyWordSpecLike

import java.nio.file.{ Files, Paths }
import scala.language.{ implicitConversions, postfixOps }

object TrackSerializationTest {
  val grid: Grid = Grid(16, 8)
  val engine: Engine = Engine(Theories from grid)
  val query: String = PrologQuery(from = grid randomInBorder LEFT, to = grid randomInBorder RIGHT)

  val listSize: Int = 6

}

class TrackSerializationTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val trackList: List[Track] =
    List
      .range(1, listSize + 1)
      .flatMap(_ =>
        List(Track(trackFromTerm(engine.solve(Term.createTerm(query)).head.getTerm("P"))))
      )

  val coder: FileCoder = FileCoder()
  val emptyList: List[Track] = List()

  "The controller" when {
    "use a FileCoder" should {
      "be able to serialize and deserialize the track list" in {
        coder.serialize(trackList)

        val list: List[Track] = coder.deserialize()

        for (i <- trackList.indices)
          trackList(i).equals(list(i)) shouldBe true
      }
    }

    "the json file contains an empty list of tracks" should {
      "return an empty list" in {
        coder.serialize(emptyList)

        val list: List[Track] = coder.deserialize()

        list.isEmpty shouldBe true
      }
    }

    "try to save a track and the file does not exist" should {
      "create a new file and save it without any error" in {
        Files.deleteIfExists(Paths.get(coder.path))

        coder.serialize(emptyList)

        Files.exists(Paths.get(coder.path)) shouldBe true
      }
    }

    "try to load the tracks list and the file does not exist" should {
      "return an empty list of tracks" in {
        Files.deleteIfExists(Paths.get(coder.path))

        val list: List[Track] = coder.deserialize()

        Files.exists(Paths.get(coder.path)) shouldBe true
        list.isEmpty shouldBe true
      }
    }

  }

}
