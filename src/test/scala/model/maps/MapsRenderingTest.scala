package model.maps

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.maps.Grids.Grid
import org.scalatest.wordspec.AnyWordSpecLike
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.layout.Pane
import MapsRenderingTest._
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import controller.GameLoop.GameLoopActor
import controller.Messages.{
  EntityUpdated,
  Input,
  Message,
  ModelUpdated,
  Render,
  RenderMap,
  Start,
  TickUpdate,
  Update,
  UpdateEntity
}
import model.Positions._
import javafx.scene.paint.ImagePattern
import model.maps.Tracks.Track
import org.scalatest.Ignore
import javafx.scene.image.Image
import model.entities.Entities.Entity
import scalafx.scene.paint.Color
import view.View.{ Board, ViewActor }
import model.entities.balloons.BalloonType._
import model.entities.balloons.Balloons.Balloon
import model.maps.Cells.Cell
import model.maps.Tracks.Directions.RIGHT
import utils.Constants

import scala.language.postfixOps
import java.io.File

object MapsRenderingTest {
  val cellSize: Int = 60
  val canvas: Canvas = new Canvas(1200, 600)

  val canvasAppBuilder: Canvas => JFXApp = canvas =>
    new JFXApp {

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

  val paneAppBuilder: Board => JFXApp = board =>
    new JFXApp {

      stage = new PrimaryStage {
        title = "Test"
        resizable = false

        scene = new Scene(Constants.width, Constants.height) {
          root = board
        }
      }
    }

  val dummyModel: (List[Entity], Track) => Behavior[Update] = (entities, track) =>
    Behaviors.setup { ctx =>
      var entitiesUpdated: List[Entity] = List()
      var gameLoop: ActorRef[Input] = null
      val actors: List[ActorRef[Update]] =
        entities map (e => ctx.spawnAnonymous(dummyBalloonActor(e.asInstanceOf[Balloon])))
      Behaviors.receiveMessage {
        case TickUpdate(elapsedTime, replyTo) =>
          gameLoop = replyTo
          actors foreach {
            _ ! UpdateEntity(elapsedTime, entities, ctx.self, track)
          }
          Behaviors.same

        case EntityUpdated(entity) =>
          entitiesUpdated = entitiesUpdated appended entity
          if (entitiesUpdated.size == entities.size) {
            gameLoop ! ModelUpdated(entitiesUpdated)
            dummyModel(entitiesUpdated, track)
          } else {
            Behaviors.same
          }

        case _ => Behaviors.same
      }
    }

  var linearPosition: Double = 0.0

  val dummyBalloonActor: Balloon => Behavior[Update] = balloon =>
    Behaviors.setup { _ =>
      Behaviors.receiveMessage { case UpdateEntity(elapsedTime, _, replyTo, track) =>
        linearPosition += 1.5 * elapsedTime
        val currentCell: Cell = track.cells(linearPosition.toInt)
        val newBalloon: Balloon = currentCell match {
          case start if start == track.start =>
            balloon.in(
              currentCell.exactPosition(RIGHT)(linearPosition - linearPosition.toInt) - (15.0, 20.0)
            )
          case _ =>
            balloon.in(
              currentCell.exactPosition(track.cells(linearPosition.toInt - 1).direction)(
                linearPosition - linearPosition.toInt
              ) - (15.0, 20.0)
            )
        }
        replyTo ! EntityUpdated(newBalloon)
        dummyBalloonActor(newBalloon)
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
          canvas.graphicsContext2D.fillRect(
            cell.x * cellSize,
            cell.y * cellSize,
            cellSize - 0.5,
            cellSize - 0.5
          )
        }
        canvasAppBuilder(canvas).main(Array())
      }
      "design grass" in {
        val img: File = new File("res/grass-pattern.png")
        Grid(20, 10).cells foreach { cell =>
          canvas.graphicsContext2D.setFill(new ImagePattern(new Image(img.toURI.toString)))
          canvas.graphicsContext2D.fillRect(
            cell.x * cellSize,
            cell.y * cellSize,
            cellSize - 0.5,
            cellSize - 0.5
          )
        }
        canvasAppBuilder(canvas).main(Array())
      }
    }
    "testing tracks" should {
      "design a red path" in {
        Track(Grid(20, 10)).cells.zipWithIndex foreach { cell =>
          canvas.graphicsContext2D.setFill(Color.Red)
          canvas.graphicsContext2D.fillRect(
            cell._1.x * cellSize,
            cell._1.y * cellSize,
            cellSize,
            cellSize
          )
          canvas.graphicsContext2D.setFill(Color.White)
          canvas.graphicsContext2D.fillText(
            cell._2.toString,
            cell._1.x * cellSize + 17.5,
            cell._1.y * cellSize + 30
          )
        }
        canvasAppBuilder(canvas).main(Array())
      }
      "design the road" in {
        ActorSystem[Message](
          Behaviors.setup[Message] { ctx =>
            val view: ActorRef[Render] = ctx.spawn(ViewActor(), "view")
            val grid: Grid = Grid(Constants.widthRatio, Constants.heightRatio)
            view ! RenderMap(Track(grid))
            Behaviors.empty
          },
          "system"
        )
        paneAppBuilder(ViewActor.board).main(Array())
      }
      "design balloons on the road" in {
        ActorSystem[Message](
          Behaviors.setup[Message] { ctx =>
            val view: ActorRef[Render] = ctx.spawn(ViewActor(), "view")
            val grid: Grid = Grid(Constants.widthRatio, Constants.heightRatio)
            val track: Track = Track(grid)
            view ! RenderMap(track)
            val entities: List[Entity] = List(Red balloon)
            val model: ActorRef[Update] = ctx.spawn(dummyModel(entities, track), "model")
            val gameLoop: ActorRef[Input] = ctx.spawn(GameLoopActor(model, view), "gameLoop")
            gameLoop ! Start()
            Behaviors.empty
          },
          "system"
        )
        paneAppBuilder(ViewActor.board).main(Array())
      }
    }
  }
}
