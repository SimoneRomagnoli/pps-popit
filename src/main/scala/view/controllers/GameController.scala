package view.controllers

import controller.Messages.{ Input, PlaceTower }
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.{ BorderPane, Pane, Region, VBox }
import scalafx.scene.shape.{ Rectangle, Shape }
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import utils.Constants.Maps.gameGrid
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.Rendering

import scala.language.reflectiveCalls
import scala.util.Random

/**
 * Controller of the game. This controller loads the game fxml file and is able to draw every
 * element of a game.
 */
trait ViewGameController {
  def loading(): Unit
  def reset(): Unit
  def setup(): Unit
  def setSend(send: Input => Unit): Unit
  def draw(grid: Grid): Unit
  def draw(track: Track): Unit
  def draw(entities: List[Entity]): Unit
}

/**
 * Controller class bound to the game fxml.
 *
 * @param gameBoard,
 *   the pane containing the game.
 * @param mapNodes,
 *   the number of nodes of the map.
 */
@sfxml
class GameController(
    val mainPane: BorderPane,
    val gameBoard: Pane,
    val gameMenu: VBox,
    @nested[GameMenuController] val gameMenuController: ViewGameMenuController,
    var mapNodes: Int = 0,
    var send: Input => Unit,
    var currentTrack: Seq[Cell] = Constants.Maps.basicTrack)
    extends ViewGameController {
  setup()
  this draw gameGrid
  loading()

  override def setup(): Unit = Platform runLater {
    setLayout(gameBoard, gameBoardWidth, gameBoardHeight)
    setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
    setTowersSelection()
    gameMenuController.setup()
  }

  override def setSend(reference: Input => Unit): Unit = send = reference

  override def draw(grid: Grid = Constants.Maps.gameGrid): Unit = Platform runLater {
    mapNodes += grid.width * grid.height
    Rendering a grid into gameBoard.children
  }

  override def loading(): Unit = Platform runLater {
    val loadingLabel: Label =
      Label(Constants.View.loadingLabels(Random.between(0, Constants.View.loadingLabels.size)))
    loadingLabel
      .layoutXProperty()
      .bind(gameBoard.widthProperty().subtract(loadingLabel.widthProperty()).divide(2))
    loadingLabel
      .layoutYProperty()
      .bind(gameBoard.heightProperty().subtract(loadingLabel.heightProperty()).divide(2))

    gameBoard.children.add(loadingLabel)
  }

  override def reset(): Unit = Platform runLater {
    gameBoard.children.clear()
    mapNodes = 0
  }

  override def draw(track: Track): Unit = Platform runLater {
    mapNodes += track.cells.size
    currentTrack = track.cells
    Rendering a track into gameBoard.children
  }

  override def draw(entities: List[Entity]): Unit = Platform runLater {
    gameBoard.children.removeRange(mapNodes, gameBoard.children.size)
    entities foreach {
      case balloon: Balloon =>
        val viewEntity: Shape = toShape(balloon, "images/balloons/RED.png")
        gameBoard.children.add(viewEntity)
      case tower: Tower[_] =>
        val viewEntity: Shape = toShape(tower, "images/" + tower.toString + ".png")
        viewEntity.rotate = Math.atan2(tower.direction.y, tower.direction.x) * 180 / Math.PI
        gameBoard.children.add(viewEntity)
      //val circle: Shape = Circle(tower.position.x, tower.position.y, tower.sightRange)
      //circle.setFill(Color.Gray.opacity(0.45))
      //gameBoard.children.add(circle)
      case bullet: Bullet =>
        val viewEntity: Shape = toShape(bullet, "/images/bullets/DART.png")
        viewEntity.rotate = Math.atan2(bullet.speed.y, bullet.speed.x) * 180 / Math.PI
        gameBoard.children.add(viewEntity)
      case _ =>
    }
  }

  private def toShape(entity: Entity, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

  private def setLayout(region: Region, width: Double, height: Double): Unit = {
    region.maxWidth = width
    region.minWidth = width
    region.maxHeight = height
    region.minHeight = height
  }

  private def setTowersSelection(): Unit = {
    gameBoard.onMouseExited = _ => removeEffects()
    gameBoard.onMouseMoved = e => {
      removeEffects()
      if (gameMenuController.anyTowerSelected()) {
        val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
        val effect: ColorAdjust = new ColorAdjust()
        val place: Node = e.getTarget.asInstanceOf[Node]
        if (selectable(cell)) {
          effect.hue = 0.12
          effect.brightness = 0.2
          place.setCursor(Cursor.Hand)
        } else {
          place.setCursor(Cursor.Default)
        }
        place.setEffect(effect)
      } else {
        e.getTarget.asInstanceOf[Node].setCursor(Cursor.Default)
      }
    }
    gameBoard.onMouseClicked = e => {
      val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
      if (gameMenuController.anyTowerSelected() && selectable(cell)) {
        removeEffects()
        gameMenuController.unselectDepot()
        send(PlaceTower(cell))
      }
    }
  }

  private def selectable(cell: Cell): Boolean =
    !currentTrack.exists(c => c.x == cell.x && c.y == cell.y)

  private def removeEffects(): Unit =
    gameBoard.children.foreach(_.setEffect(null))
}
