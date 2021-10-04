package view.controllers

import controller.Messages.{ Input, PlaceTower }
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.{ BorderPane, Pane, Region, VBox }
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import utils.Constants.Maps.gameGrid
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.render.Drawings.Drawing
import view.render.Rendering

import scala.language.reflectiveCalls
import scala.util.Random

/**
 * Controller of the game. This controller loads the game fxml file and is able to draw every
 * element of a game.
 */
trait ViewGameController extends ViewController {
  def loading(): Unit
  def reset(): Unit
  def setup(): Unit
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
    var occupiedCells: Seq[Cell] = Seq())
    extends ViewGameController {
  setup()
  this draw gameGrid
  loading()
  val drawing: Drawing = Drawing()
  val bulletPic: ImagePattern = new ImagePattern(new Image("images/bullets/DART.png"))

  override def setup(): Unit = Platform runLater {
    setLayout(gameBoard, gameBoardWidth, gameBoardHeight)
    setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
    setTowersSelection()
    gameMenuController.setup()
  }

  override def setSend(reference: Input => Unit): Unit = {
    send = reference
    gameMenuController.setSend(reference)
  }

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
    occupiedCells = track.cells
    Rendering a track into gameBoard.children
  }

  override def draw(entities: List[Entity]): Unit = Platform runLater {
    gameBoard.children.removeRange(mapNodes, gameBoard.children.size)
    entities foreach (entity => Rendering an entity into gameBoard.children)
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
      if (gameMenuController.anyTowerSelected() && !gameMenuController.isPaused) {
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
      if (gameMenuController
          .anyTowerSelected() && selectable(cell) && !gameMenuController.isPaused) {
        removeEffects()
        gameMenuController.unselectDepot()
        occupiedCells = occupiedCells :+ cell
        send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
      }
    }
  }

  private def selectable(cell: Cell): Boolean =
    !occupiedCells.exists(c => c.x == cell.x && c.y == cell.y)

  private def removeEffects(): Unit =
    gameBoard.children.foreach(_.setEffect(null))
}
