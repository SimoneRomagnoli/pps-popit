package model.maps

import alice.tuprolog.SolveInfo
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
  val cell: Cell = GridCell(1, 1, NONE)
  val upCell: Cell = GridCell(1, 0, NONE)
  val downCell: Cell = GridCell(1, 2, NONE)
  val leftCell: Cell = GridCell(0, 1, NONE)
  val rightCell: Cell = GridCell(2, 1, NONE)

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
  val query: String = PrologQuery(from = grid randomInBorder LEFT, to = grid randomInBorder RIGHT)

  val iterator: Iterator[SolveInfo] = engine.solve(query).iterator

}

class MapsTest extends AnyWordSpec with Matchers {

  "The Cells" when {
    "in a grid" should {
      "have no neighbors" in {
        (cell nextOnTrack) shouldBe cell
      }
      "have neighbors" in {
        (cell direct UP nextOnTrack) shouldBe upCell
        (cell direct DOWN nextOnTrack) shouldBe downCell
        (cell direct LEFT nextOnTrack) shouldBe leftCell
        (cell direct RIGHT nextOnTrack) shouldBe rightCell
      }
      "change direction" in {
        (cell direct UP).turnLeft().direction shouldBe LEFT
        (cell direct UP).turnRight().direction shouldBe RIGHT
      }
    }
  }

  "The Directions" when {
    "created" should {
      "have opposites" in {
        (UP opposite) shouldBe DOWN
        (DOWN opposite) shouldBe UP
        (RIGHT opposite) shouldBe LEFT
        (LEFT opposite) shouldBe RIGHT
        (NONE opposite) shouldBe NONE
      }
      "turn left" in {
        (UP turnLeft) shouldBe LEFT
        (DOWN turnLeft) shouldBe RIGHT
        (RIGHT turnLeft) shouldBe UP
        (LEFT turnLeft) shouldBe DOWN
        (NONE turnLeft) shouldBe NONE
      }
      "turn right" in {
        (UP turnRight) shouldBe RIGHT
        (DOWN turnRight) shouldBe LEFT
        (RIGHT turnRight) shouldBe DOWN
        (LEFT turnRight) shouldBe UP
        (NONE turnRight) shouldBe NONE
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
        Grid(3, 3).cells.forall(_.direction == NONE) shouldBe true
      }
      "have borders" in {
        val grid: Grid = Grid(2, 2)
        grid.border(UP) shouldBe Seq(Cell(0, 0), Cell(1, 0))
        grid.border(LEFT) shouldBe Seq(Cell(0, 0), Cell(0, 1))
        grid.border(RIGHT) shouldBe Seq(Cell(1, 0), Cell(1, 1))
        grid.border(DOWN) shouldBe Seq(Cell(0, 1), Cell(1, 1))
        grid.border(NONE) shouldBe grid.cells
      }
    }
  }

  "The Plotter" when {
    "given a start and an end borders" should {
      "plot a track by forming a query" in {
        val plotter: Plotter = PrologPlotter() in grid startingFrom LEFT endingAt RIGHT
        val track: Track = Track(plotter plot)
        track.start.x shouldBe 0
        track.finish.x shouldBe (grid.width - 1)
      }
    }
  }

  "The Tracks" when {
    "created with prolog" should {
      "return a track" in {
        engine.solve(query).length shouldBe Int.MaxValue
        Solutions
          .trackFromPrologSolution(engine.solve(query)(1))
          .isInstanceOf[Seq[Cell]] shouldBe true
      }
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
  }
}
