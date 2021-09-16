package maps

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import maps.MapsTest._
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.NONE
import model.maps.Tracks.Track
import org.scalatest.wordspec.AnyWordSpecLike
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color

import scala.language.postfixOps

object MapsTest {
  val singleGrid: Seq[Cell] = Seq(Cell(0, 0))
  val threeForThreeGrid: Seq[Cell] = Seq(Cell(0, 0), Cell(0, 1), Cell(0, 2),
    Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 0), Cell(2, 1), Cell(2, 2))
  val threeForThreeArea: Int = 9
  val grid: Grid = Grid(40, 40)
  val canvas: Canvas = new Canvas(1200, 600)
  val cellSize: Int = 60
  val appBuilder: Unit => JFXApp = _ => new JFXApp {
    stage = new PrimaryStage {
      title = "Test"
      scene = new Scene(1200, 600) {
        root = new Pane {
          this.setStyle("-fx-background-color: #000000")
          children = Seq(canvas)
        }
      }
    }
  }
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
    "applied to the view" should {
      "design squares" in {
        Grid(20, 10).cells foreach { cell =>
          canvas.graphicsContext2D.setFill(Color.White)
          canvas.graphicsContext2D.fillRect(cell.x*cellSize, cell.y*cellSize, cellSize-0.5, cellSize-0.5)
        }
        appBuilder().main(Array())
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

      }
    }
    "applied to the view" should {
      "design a red path" in {
        val track: Track = Track(Grid(20, 10))
        track.cells.zipWithIndex.foreach { c =>
          println(c._1+" "+c._2)
        }
        track.cells.zipWithIndex foreach { cell =>
          canvas.graphicsContext2D.setFill(Color.Red)
          canvas.graphicsContext2D.fillRect(cell._1.x*cellSize, cell._1.y*cellSize, cellSize, cellSize)
          canvas.graphicsContext2D.setFill(Color.White)
          canvas.graphicsContext2D.fillText(cell._2.toString, cell._1.x*cellSize+17.5, cell._1.y*cellSize+30)
        }
        appBuilder().main(Array())
      }
    }
  }
}
