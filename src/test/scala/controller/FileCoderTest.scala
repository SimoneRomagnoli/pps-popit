package controller

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import alice.tuprolog.Term
import controller.FileCoderTest.{ engine, listSize, query }
import controller.inout.FileCoder
import controller.inout.FileCoders.{ trackEncoder, CoderBuilder }
import controller.inout.FileCoders.CoderBuilder.{ appDir, filesDir, imagesDir, jsonPath }
import controller.settings.Settings.Normal
import io.circe.syntax.EncoderOps
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.{ Left, Right }
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Engines.Engine
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm
import model.maps.prolog.PrologUtils.Theories
import org.scalatest.DoNotDiscover
import org.scalatest.wordspec.AnyWordSpecLike

import java.nio.file.{ Files, Paths }
import scala.language.{ implicitConversions, postfixOps }
import scala.reflect.io.Path

object FileCoderTest {
  val grid: Grid = Grid(16, 8)
  val engine: Engine = Engine(Theories from grid)

  val query: String =
    PrologQuery(from = grid randomInBorder Left, to = grid randomInBorder Right, Normal)

  val listSize: Int = 6

}

@DoNotDiscover
class FileCoderTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val trackList: List[Track] =
    List
      .range(1, listSize + 1)
      .flatMap(_ =>
        List(Track(trackFromTerm(engine.solve(Term.createTerm(query)).head.getTerm("P"))))
      )

  Path(appDir).deleteRecursively() // Clean directories tree to setup testing environment
  Files.notExists(Paths.get(appDir)) shouldBe true
  var coder: FileCoder = FileCoder()

  val emptyList: List[Track] = List()

  "The FileCoder" when {
    "it just spawned" should {
      "create the directory tree and a json file with an empty tracks list" in {
        Files.exists(Paths.get(filesDir)) shouldBe true
        Files.exists(Paths.get(imagesDir)) shouldBe true
        Files.exists(Paths.get(jsonPath)) shouldBe true
        coder.deserialize().isEmpty shouldBe true
      }
    }
    "it is called" should {
      "be able to serialize and deserialize the same list of tracks" in {
        coder.serialize(trackList)

        val list: List[Track] = coder.deserialize()

        for (i <- trackList.indices)
          trackList(i).equals(list(i)) shouldBe true
      }
      "be able to handle the track list in case of empty list" in {
        coder.serialize(emptyList)

        val list: List[Track] = coder.deserialize()

        list.isEmpty shouldBe true
      }
      "be able to restore the directories tree and the json file" in {
        coder.clean()
        Files.exists(Paths.get(filesDir)) shouldBe true
        Files.exists(Paths.get(imagesDir)) shouldBe true
        Files.exists(Paths.get(jsonPath)) shouldBe true
        coder.deserialize().isEmpty shouldBe true
        Files.list(Paths.get(imagesDir)).findFirst().isEmpty shouldBe true
      }
    }
  }

}
