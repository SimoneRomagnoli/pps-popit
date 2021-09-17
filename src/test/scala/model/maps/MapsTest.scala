package model.maps

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.maps.MapsTest._
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.NONE
import model.maps.Tracks.Track
import org.scalatest.wordspec.AnyWordSpecLike

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
  val grid: Grid = Grid(40, 40)
}

class MapsTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

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
        Track(grid).cells.size should be >= grid.width
      }
      "have directions" in {
        Track(grid).cells.forall(_.direction != NONE) shouldBe true
      }
      "do not repeat" in {
        val track: Track = Track(grid)
        track.cells.foreach { cell =>
          track.cells.count(c => c.x == cell.x && c.y == cell.y) shouldBe 1
        }
      }
    }
  }
}
