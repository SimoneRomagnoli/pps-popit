package maps

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.maps.Grids.Grid
import org.scalatest.wordspec.AnyWordSpecLike
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane
import MapsRenderingTest._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{Message, Render, RenderMap}
import javafx.scene.paint.ImagePattern
import model.maps.Tracks.Track
import org.scalatest.Ignore
import javafx.scene.image.Image
import scalafx.scene.paint.Color
import view.View.ViewActor

import java.io.File

object MapsRenderingTest {
  val cellSize: Int = 60
  val canvas: Canvas = new Canvas(1200, 600)
  val appBuilder: Canvas => JFXApp = canvas => new JFXApp {
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

@Ignore
class MapsRenderingTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "The Rendering" when {
    "testing grids" should {
      "design squares" in {
        Grid(20, 10).cells foreach { cell =>
          canvas.graphicsContext2D.setFill(Color.White)
          canvas.graphicsContext2D.fillRect(cell.x*cellSize, cell.y*cellSize, cellSize-0.5, cellSize-0.5)
        }
        appBuilder(canvas).main(Array())
      }
      "design grass" in {
        val img: File = new File("res/grass-pattern.png")
        Grid(20, 10).cells foreach { cell =>
          canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
          canvas.graphicsContext2D.fillRect(cell.x*cellSize, cell.y*cellSize, cellSize-0.5, cellSize-0.5)
        }
        appBuilder(canvas).main(Array())
      }
    }
    "testing tracks" should {
      "design a red path" in {
        Track(Grid(20, 10)).cells.zipWithIndex foreach { cell =>
          canvas.graphicsContext2D.setFill(Color.Red)
          canvas.graphicsContext2D.fillRect(cell._1.x*cellSize, cell._1.y*cellSize, cellSize, cellSize)
          canvas.graphicsContext2D.setFill(Color.White)
          canvas.graphicsContext2D.fillText(cell._2.toString, cell._1.x*cellSize+17.5, cell._1.y*cellSize+30)
        }
        appBuilder(canvas).main(Array())
      }
      "design the road" in {
        ActorSystem[Message](Behaviors.setup[Message] { ctx =>
          val view: ActorRef[Render] = ctx.spawn(ViewActor(), "view")
          val grid: Grid = Grid(16, 8)
          view ! RenderMap(grid, Track(grid))
          Behaviors.empty
        }, "system")
        appBuilder(ViewActor.canvas).main(Array())
      }
    }
  }
}
