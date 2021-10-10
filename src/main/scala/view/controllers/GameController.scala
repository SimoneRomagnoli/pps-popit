package view.controllers

import cats.effect.IO
import controller.Messages.{ Input, Message, PlaceTower, TowerIn, TowerOption }
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.towers.Towers.Tower
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.control.Label
import scalafx.scene.layout.{ BorderPane, Pane, VBox }
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import utils.Constants.Maps.gameGrid
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.render.Drawings.Drawing
import view.render.Rendering

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.{ implicitConversions, reflectiveCalls }
import scala.util.{ Failure, Random, Success }

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
    var highlightNodes: Int = 0,
    var send: Input => Unit,
    var ask: Message => Future[Message],
    var occupiedCells: Seq[Cell] = Seq())
    extends ViewGameController {

  import GameUtilities._
  import MouseEvents._

  val drawing: Drawing = Drawing()
  val bulletPic: ImagePattern = new ImagePattern(new Image("images/bullets/DART.png"))
  setup()

  override def setup(): Unit = Platform runLater {
    this draw gameGrid
    loading()
    Rendering.setLayout(gameBoard, gameBoardWidth, gameBoardHeight)
    Rendering.setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
    setMouseHandlers()
    gameMenuController.setup()
    gameMenuController.setHighlightingTower(highlight)
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
    gameBoard.children.removeRange(mapNodes + highlightNodes, gameBoard.children.size)
    entities foreach (entity => Rendering an entity into gameBoard.children)
  }

  private object GameUtilities {

    def highlight(tower: Tower[_], insertion: Boolean): Unit =
      if (insertion) {
        highlightNodes = 1
        gameBoard.children.removeRange(mapNodes, gameBoard.children.size)
        Rendering sightOf tower into gameBoard.children
      } else {
        highlightNodes = 0
      }

    def removeEffects(): Unit =
      gameBoard.children.foreach(_.setEffect(null))
  }

  private object MouseEvents {
    import InputEventHandlers._

    def setMouseHandlers(): Unit = {
      gameBoard.onMouseExited = _ => removeEffects()
      gameBoard.onMouseMoved = MouseEvents.move(_).unsafeRunSync()
      gameBoard.onMouseClicked = MouseEvents.click(_).unsafeRunSync()
    }

    def click(e: MouseEvent): IO[Unit] = for {
      _ <-
        if (!gameMenuController.isPaused && !gameMenuController.anyTowerSelected())
          clickedTower(e, ask, gameMenuController.fillTowerStatus)
        else
          IO.unit
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
    } yield ()

    private def placeTower(e: MouseEvent): IO[Unit] = {
      val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
      ask(TowerIn(cell)).onComplete {
        case Success(value) =>
          value.asInstanceOf[TowerOption] match {
            case TowerOption(option) =>
              option match {
                case Some(_) =>
                case _ =>
                  Platform runLater {
                    removeEffects()
                    gameMenuController.unselectDepot()
                    occupy(cell)
                    send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
                  }
              }
          }
        case Failure(exception) => println(exception)
      }
    }

    private def occupy(cell: Cell): Unit =
      occupiedCells = occupiedCells :+ cell
  }
}
