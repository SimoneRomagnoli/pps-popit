package view.controllers

import cats.effect.IO
import controller.Controller.ControllerMessages.{ NewTrack, PlaceTower }
import controller.Messages.{ Input, Message }
import javafx.scene.input.MouseEvent
import model.entities.Entities.Entity
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.{ Selectable, Selected }
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Cursor
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout._
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import utils.Constants.Maps.gameGrid
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.render.Animations.Animations
import view.render.Drawings.{ Drawing, GameDrawings }
import view.render.{ Animating, Rendering }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.{ implicitConversions, reflectiveCalls }
import scala.util.{ Failure, Success }

/**
 * Controller of the game. This controller loads the game fxml file and is able to draw every
 * element of a game.
 */
trait ViewGameController extends ViewController {
  def reset(): Unit
  def setup(): Unit
  def render(stats: GameStats): Unit
  def draw(grid: Grid): Unit
  def draw(track: Track): Unit
  def draw(entities: List[Entity]): Unit
  def animate(entity: Entity): Unit
  def gameMenuController: ViewGameMenuController
}

/**
 * Controller class bound to the game fxml.
 */
@sfxml
class GameController(
    val mainPane: BorderPane,
    val gameBoard: StackPane,
    val trackPane: Pane,
    val highlightPane: Pane,
    val trackChoicePane: HBox,
    val entitiesPane: Pane,
    val animationsPane: Pane,
    val gameMenu: VBox,
    val trackChoiceVerticalContainer: VBox,
    val trackChoiceContainer: VBox,
    val keepTrack: ToggleButton,
    val changeTrack: ToggleButton,
    @nested[GameMenuController] val gameMenuController: ViewGameMenuController,
    var send: Input => Unit,
    var ask: Message => Future[Message],
    var occupiedCells: Seq[Cell] = Seq())
    extends ViewGameController {

  import GameUtilities._
  import MouseEvents._
  val images: Animations = Animations()
  val drawing: Drawing = Drawing(GameDrawings())
  setup()

  override def setup(): Unit = Platform runLater {
    resetAll()
    Rendering a gameGrid into trackPane.children
    setLayouts()
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

  override def show(): Unit = mainPane.visible = true
  override def hide(): Unit = mainPane.visible = false

  override def reset(): Unit = Platform runLater {
    resetAll()
  }

  override def render(stats: GameStats): Unit = Platform runLater {
    gameMenuController renderStats stats
  }

  override def draw(grid: Grid = Constants.Maps.gameGrid): Unit = Platform runLater {
    Rendering a grid into trackPane.children
  }

  override def draw(track: Track): Unit = Platform runLater {
    occupiedCells = track.cells
    Rendering a track into trackPane.children
    trackChoicePane.visible = true
    trackChoiceContainer.visible = true
  }

  override def draw(entities: List[Entity]): Unit = Platform runLater {
    entitiesPane.children.clear()
    entities foreach (entity => Rendering an entity into entitiesPane.children)
  }

  override def animate(entity: Entity): Unit = Platform runLater {
    Animating an entity into animationsPane.children
  }

  private object GameUtilities {

    def setLayouts(): Unit = {
      Rendering.setLayout(gameBoard, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(trackPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(trackChoicePane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(highlightPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(entitiesPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(animationsPane, gameBoardWidth, gameBoardHeight)
      entitiesPane.setMouseTransparent(true)
      animationsPane.setMouseTransparent(true)
      trackChoicePane.setMouseTransparent(false)
      highlightPane.setMouseTransparent(true)
      trackChoicePane.setPickOnBounds(false)
      trackChoicePane.visible = false
      gameMenuController.disableAllButtons()
      Rendering.setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
      trackChoiceVerticalContainer.setAlignment(Pos.Center)
    }

    def resetAll(): Unit = {
      trackPane.children.clear()
      highlightPane.children.clear()
      trackChoicePane.children.removeRange(3, trackChoicePane.children.size)
      entitiesPane.children.clear()
      animationsPane.children.clear()
    }

    def highlight(tower: Option[Tower[_]]): Unit = {
      highlightPane.children.clear()
      if (tower.isDefined) {
        Rendering sightOf tower.get into highlightPane.children
      }
    }

    def removeEffects(): Unit =
      trackPane.children.foreach(_.setEffect(null))
  }

  private object MouseEvents {
    import InputEventHandlers._

    def setMouseHandlers(): Unit = {
      keepTrack.onMouseClicked = _ => {
        gameMenuController.enableAllButtons()
        trackChoiceContainer.visible = false
        trackChoicePane.setMouseTransparent(true)
      }
      changeTrack.onMouseClicked = _ => {
        send(NewTrack())
        trackPane.children.clear()
        Rendering a gameGrid into trackPane.children
        trackChoiceContainer.visible = false
      }
      trackPane.onMouseExited = _ => removeEffects()
      trackPane.onMouseMoved = MouseEvents.move(_).unsafeRunSync()
      trackPane.onMouseClicked = MouseEvents.click(_).unsafeRunSync()
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
      ask(Selectable(cell)).onComplete {
        case Success(value) =>
          value.asInstanceOf[Selected] match {
            case Selected(selectable) =>
              if (selectable) {
                Platform runLater {
                  removeEffects()
                  occupiedCells = occupiedCells :+ cell
                  gameMenuController.unselectDepot()
                  send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
                }
              } else {}
          }
        case Failure(exception) => println(exception)
      }
    }
  }
}
