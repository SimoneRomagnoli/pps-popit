package maps

import alice.tuprolog.{SolveInfo, Term}
import maps.MapsTest._
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Scala2P._
import model.maps.Tracks.Directions.NONE
import model.maps.Tracks.Track
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.SeqView
import scala.language.postfixOps

object MapsTest {
  val singleGrid: Seq[Cell] = Seq(Cell(0, 0))
  val threeForThreeGrid: Seq[Cell] = Seq(Cell(0, 0), Cell(0, 1), Cell(0, 2),
    Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 0), Cell(2, 1), Cell(2, 2))
  val threeForThreeArea: Int = 9
  val suitableWidth: Int = 16
  val engine: Term => SeqView[SolveInfo] = mkPrologEngine("res/grids.pl")
  val iterator: Iterator[SolveInfo] = engine("allPath(c(0,3), c(15,3), P).").iterator
}

class MapsTest extends AnyWordSpec with Matchers {
  "The Grids" when {
    "just created" should {
      "be made of cells" in {
        (Grid(1, 1) cells) shouldBe singleGrid
        (Grid(3, 3) cells) shouldBe threeForThreeGrid
        Grid(3, 3).cells.size shouldBe threeForThreeArea
      }
      "have no directions" in {
        Grid(3, 3).cells.forall(_.direction == NONE) shouldBe true
      }
    }
  }
  "The Tracks" when {
    "just created" should {
      "be long enough" in {
        getTrack(iterator.next()).cells.size should be >= suitableWidth
      }
      "have directions" in {
        getTrack(iterator.next()).cells.forall(_.direction != NONE) shouldBe true
      }
      "do not repeat" in {
        val track: Track = getTrack(iterator.next())
        track.cells.foreach { cell =>
          track.cells.count(c => c.x == cell.x && c.y == cell.y) shouldBe 1
        }
      }
    }
  }
}
