package model.maps

import alice.tuprolog.SolveInfo
import controller.settings.Settings.Normal
import model.Positions.Vector2D
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.MapsTest._
import model.maps.Plots.{ Plotter, PrologPlotter }
import model.maps.Tracks.Directions._
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Engines._
import model.maps.prolog.PrologUtils.Queries.PrologQuery
import model.maps.prolog.PrologUtils.{ Solutions, Theories }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

object MapsTest {
  val cell: Cell = GridCell(1, 1, None)
  val upCell: Cell = GridCell(1, 0, None)
  val downCell: Cell = GridCell(1, 2, None)
  val leftCell: Cell = GridCell(0, 1, None)
  val rightCell: Cell = GridCell(2, 1, None)

  val topPosition: Vector2D = (0.0, 0.0)

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

  val query: String =
    PrologQuery(from = grid randomInBorder Left, to = grid randomInBorder Right, Normal)

  val supplyTrack: () => SolveInfo = () => engine.solve(query).head

}

class MapsTest extends AnyWordSpec with Matchers {

  "The Cells" when {
    "in a grid" should {
      "have no neighbors" in {
        (cell nextOnTrack) shouldBe cell
      }
      "have neighbors" in {
        (cell direct Up nextOnTrack) shouldBe upCell
        (cell direct Down nextOnTrack) shouldBe downCell
        (cell direct Left nextOnTrack) shouldBe leftCell
        (cell direct Right nextOnTrack) shouldBe rightCell
      }
      "change direction" in {
        (cell direct Up).turnLeft().direction shouldBe Left
        (cell direct Up).turnRight().direction shouldBe Right
      }
    }
  }

  "The Directions" when {
    "created" should {
      "have opposites" in {
        (Up opposite) shouldBe Down
        (Down opposite) shouldBe Up
        (Right opposite) shouldBe Left
        (Left opposite) shouldBe Right
        (None opposite) shouldBe None
      }
      "turn left" in {
        (Up turnLeft) shouldBe Left
        (Down turnLeft) shouldBe Right
        (Right turnLeft) shouldBe Up
        (Left turnLeft) shouldBe Down
        (None turnLeft) shouldBe None
      }
      "turn right" in {
        (Up turnRight) shouldBe Right
        (Down turnRight) shouldBe Left
        (Right turnRight) shouldBe Down
        (Left turnRight) shouldBe Up
        (None turnRight) shouldBe None
      }
    }
  }

  "The Grids" when {
    "just created" should {
      "be made of cells" in {
        (Grid(1, 1) cells) shouldBe singleGrid
        (Grid(3, 3) cells) shouldBe threeForThreeGrid
        Grid(3, 3).cells.size shouldBe threeForThreeArea
      }
      "have no directions" in {
        Grid(3, 3).cells.forall(_.direction == None) shouldBe true
      }
      "have borders" in {
        val grid: Grid = Grid(2, 2)
        grid.border(Up) shouldBe Seq(Cell(0, 0), Cell(1, 0))
        grid.border(Left) shouldBe Seq(Cell(0, 0), Cell(0, 1))
        grid.border(Right) shouldBe Seq(Cell(1, 0), Cell(1, 1))
        grid.border(Down) shouldBe Seq(Cell(0, 1), Cell(1, 1))
        grid.border(None) shouldBe grid.cells
      }
    }
  }

  "The Plotter" when {
    "given a start and an end borders" should {
      "plot a track by forming a query" in {
        val plotter: Plotter = PrologPlotter() in grid startingFrom Left endingAt Right
        val track: Track = Track(plotter plot Normal)
        track.start.x shouldBe 0
        track.finish.x shouldBe (grid.width - 1)
      }
    }
  }

  "The Tracks" when {
    "created with prolog" should {
      "return a track" in {
        Solutions
          .trackFromPrologSolution(engine.solve(query).head)
          .isInstanceOf[Seq[Cell]] shouldBe true
      }
      "be long enough" in {
        Solutions.trackFromPrologSolution(supplyTrack()).size should be >= grid.width
      }
      "have directions" in {
        Solutions.trackFromPrologSolution(supplyTrack()).forall(_.direction != None) shouldBe true
      }
      "not repeat" in {
        val track: Seq[Cell] = Solutions.trackFromPrologSolution(supplyTrack())
        track.foreach { cell =>
          track.count(c => c.x == cell.x && c.y == cell.y) shouldBe 1
        }
      }
    }
  }
}
