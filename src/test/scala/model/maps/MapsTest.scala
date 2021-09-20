package model.maps

import alice.tuprolog.SolveInfo
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.MapsTest._
import model.maps.Tracks.Directions.{ LEFT, NONE, RIGHT }
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Engines._
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.{ Solutions, Theories }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

object MapsTest {
  val singleGrid: Seq[Cell] = Seq(Cell(0, 0))

  val threeForThreeGrid: Seq[Cell] = Seq(
    Cell(0, 0),
    Cell(0, 1),
    Cell(0, 2),
    Cell(1, 0),
    Cell(1, 1),
    Cell(1, 2),
    Cell(2, 0),
    Cell(2, 1),
    Cell(2, 2)
  )
  val threeForThreeArea: Int = 9

  val grid: Grid = Grid(16, 8)
  val engine: Engine = Engine(Theories from grid)

  val iterator: Iterator[SolveInfo] = engine
    .solve(PrologQuery(from = grid randomInBorder LEFT, to = grid randomInBorder RIGHT))
    .iterator
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
    "created with prolog" should {
      "be long enough" in {
        Solutions.trackFromPrologSolution(iterator.next()).size should be >= grid.width
      }
      "have directions" in {
        Solutions.trackFromPrologSolution(iterator.next()).forall(_.direction != NONE) shouldBe true
      }
      "not repeat" in {
        val track: Seq[Cell] = Solutions.trackFromPrologSolution(iterator.next())
        track.foreach { cell =>
          track.count(c => c.x == cell.x && c.y == cell.y) shouldBe 1
        }
      }
    }

    "created from object" should {
      "be long enough" in {
        Track(grid).cells.size should be >= grid.width
      }
      "have directions" in {
        Track(grid).cells.forall(_.direction != NONE) shouldBe true
      }
      "not repeat" in {
        val track: Track = Track(grid)
        track.cells.foreach { cell =>
          track.cells.count(c => c.x == cell.x && c.y == cell.y) shouldBe 1
        }
      }
    }
  }
}
