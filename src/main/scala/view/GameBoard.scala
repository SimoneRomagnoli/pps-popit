package view

import javafx.fxml.FXMLLoader
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.application.Platform
import scalafx.scene.control.Label
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.{ Circle, Rectangle, Shape }
import utils.Constants
import utils.Constants.Screen.cellSize

import java.io.File
import scala.util.Random

object GameBoard {

  case class Board(var mapNodes: Int = 0) extends Pane {
    children = Seq()
    drawGrid()

    def drawGrid(): Unit = Platform.runLater {
      val grid: Grid = Grid(Constants.Screen.widthRatio, Constants.Screen.heightRatio)
      mapNodes += grid.width * grid.height
      grid.cells foreach { cell =>
        val rect: Rectangle =
          Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
        rect.setFill(new ImagePattern(new Image("images/backgrounds/GRASS.png")))

        children.add(rect)
      }
    }

    def loading(): Unit = Platform.runLater {
      val loadingLabel: Label =
        Label(Constants.View.loadingLabels(Random.between(0, Constants.View.loadingLabels.size)))
      loadingLabel.setStyle("-fx-font-weight:bold; -fx-font-size:25px;")
      loadingLabel
        .layoutXProperty()
        .bind(this.widthProperty().subtract(loadingLabel.widthProperty()).divide(2))
      loadingLabel
        .layoutYProperty()
        .bind(this.heightProperty().subtract(loadingLabel.heightProperty()).divide(2))

      children.add(loadingLabel)
    }

    def reset(): Unit = Platform.runLater {
      children.clear()
      mapNodes = 0
    }

    def draw(track: Track): Unit = Platform.runLater {
      mapNodes += track.cells.size
      track.cells.prepended(GridCell(-1, 0, RIGHT)).sliding(2).foreach { couple =>
        val name: String =
          couple.head.direction.toString + "-" + couple.last.direction.toString + ".png"
        val cell: Cell = couple.last
        val rect: Rectangle = Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
        rect.setFill(new ImagePattern(new Image("images/roads/" + name)))
        children.add(rect)
      }
    }

    def draw(entities: List[Any]): Unit = Platform.runLater {
      children.removeRange(mapNodes, children.size)
      entities foreach {
        case balloon: Balloon =>
          val viewEntity: Shape = toShape(balloon)
          viewEntity.setFill(new ImagePattern(new Image("images/balloons/RED.png")))
          children.add(viewEntity)
        case tower: Tower =>
          val viewEntity: Shape = toShape(tower)
          viewEntity.setFill(new ImagePattern(new Image("images/towers/MONKEY.png")))
          viewEntity.rotate = Math.atan2(tower.direction.y, tower.direction.x) * 180 / Math.PI
          children.add(viewEntity)
          val circle: Shape = Circle(tower.position.x, tower.position.y, tower.sightRange)
          circle.setFill(Color.Gray.opacity(0.45))
          children.add(circle)
        case bullet: Bullet =>
          val img: File = new File("src/main/resources/images/bullets/DART.png")
          val viewEntity: Shape = toShape(bullet)
          viewEntity.setFill(new ImagePattern(new Image(img.toURI.toString)))
          viewEntity.rotate = Math.atan2(bullet.speed.y, bullet.speed.x) * 180 / Math.PI
          children.add(viewEntity)
        case _ =>
      }
    }

    def toShape(entity: Entity): Shape =
      Rectangle(
        entity.position.x - entity.boundary._1 / 2,
        entity.position.y - entity.boundary._2 / 2,
        entity.boundary._1,
        entity.boundary._2
      )
  }
}
