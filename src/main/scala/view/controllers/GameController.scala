package view.controllers

import cats.effect.IO
import controller.Messages.{ Input, Message, PlaceTower }
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.layout.{ BorderPane, Pane, Region, VBox }
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import utils.Constants.Maps.gameGrid
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.render.Drawings.Drawing
import view.render.Rendering

import scala.concurrent.Future
import scala.language.{ implicitConversions, reflectiveCalls }
import scala.util.Random

/**
 * Controller of the game. This controller loads the game fxml file and is able to draw every
 * element of a game.
 */
trait ViewGameController extends ViewController {
  def loading(): Unit
  def reset(): Unit
  def setup(): Unit
  def update(stats: GameStats): Unit
  def draw(grid: Grid): Unit
  def draw(track: Track): Unit
  def draw(entities: List[Entity]): Unit
}

/**
 * Controller class bound to the game fxml.
 */
@sfxml
class GameController(
    val mainPane: BorderPane,
    val gameBoard: Pane,
    val gameMenu: VBox,
    @nested[GameMenuController] val gameMenuController: ViewGameMenuController,
    var mapNodes: Int = 0,
    var send: Input => Unit,
    var ask: Message => Future[Message],
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
    setMouseHandlers()
    gameMenuController.setup()
  }

  override def setSend(reference: Input => Unit): Unit = {
    send = reference
    gameMenuController.setSend(reference)
  }

  override def setAsk(reference: Message => Future[Message]): Unit = {
    ask = reference
    gameMenuController.setAsk(reference)
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

  override def update(stats: GameStats): Unit = Platform runLater {
    gameMenuController update stats
  }

  override def draw(grid: Grid = Constants.Maps.gameGrid): Unit = Platform runLater {
    mapNodes += grid.width * grid.height
    Rendering a grid into gameBoard.children
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

  private def setMouseHandlers(): Unit = {
    gameBoard.onMouseExited = _ => removeEffects()
    gameBoard.onMouseMoved = MouseEvents.move(_).unsafeRunSync()
    gameBoard.onMouseClicked = MouseEvents.click(_).unsafeRunSync()
  }

  private def removeEffects(): Unit =
    gameBoard.children.foreach(_.setEffect(null))

  private object MouseEvents {
    import InputEventHandlers._

    def click(e: MouseEvent): IO[Unit] = for {
      _ <-
        if (!gameMenuController.isPaused && gameMenuController.anyTowerSelected())
          placeTower(e)
        else IO.unit

    } yield ()

    def move(e: MouseEvent): IO[Unit] = for {
      _ <- removeEffects()
      _ <-
        if (!gameMenuController.isPaused && gameMenuController.anyTowerSelected())
          hoverCell(e, occupiedCells)
        else {
          e.getTarget.setCursor(Cursor.Default)
          IO.unit
        }
      _ <-
        if (!gameMenuController.isPaused && !gameMenuController.anyTowerSelected())
          hoverTower(e, ask)
        else
          IO.unit

    } yield ()

    private def placeTower(e: MouseEvent): IO[Unit] = {
      val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
      if (selectable(cell)) {
        for {
          _ <- removeEffects()
          _ <- gameMenuController.unselectDepot()
          _ <- occupy(cell)
          _ <- send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
        } yield ()
      } else IO.unit
    }

    private def selectable(cell: Cell): Boolean =
      !occupiedCells.exists(c => c.x == cell.x && c.y == cell.y)

    private def occupy(cell: Cell): Unit =
      occupiedCells = occupiedCells :+ cell
  }
}
