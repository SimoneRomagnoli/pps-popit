package view.controllers

import controller.Controller.ControllerMessages.PlaceTower
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
import scalafx.scene.Cursor
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
  def newTrack(): Unit
  def render(stats: GameStats): Unit
  def draw(grid: Grid): Unit
  def draw(track: Track): Unit
  def draw(entities: List[Entity]): Unit
  def animate(entity: Entity): Unit
  def gameMenuController: ViewGameMenuController
}

trait GameControllerChild extends ViewController {
  def setParent(controller: ViewGameController): Unit
  def setLayout(): Unit
  def setTransparency(): Unit
  def reset(): Unit
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
    val entitiesPane: Pane,
    val animationsPane: Pane,
    val gameMenu: VBox,
    val trackChoice: HBox,
    val gameOver: HBox,
    @nested[TrackChoiceController] val trackChoiceController: ViewTrackChoiceController,
    @nested[GameOverController] val gameOverController: ViewGameOverController,
    @nested[GameMenuController] val gameMenuController: ViewGameMenuController,
    var send: Input => Unit,
    var ask: Message => Future[Message])
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
    setChildren()
    setTransparency()
    setMouseHandlers()
    gameMenuController.setup()
    gameMenuController.setHighlightingTower(highlight)
  }

  override def setSend(reference: Input => Unit): Unit = {
    send = reference
    trackChoiceController.setSend(reference)
    gameMenuController.setSend(reference)
    gameOverController.setSend(reference)
  }

  override def setAsk(reference: Message => Future[Message]): Unit = {
    ask = reference
    trackChoiceController.setAsk(reference)
    gameMenuController.setAsk(reference)
    gameOverController.setAsk(reference)
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
    Rendering a track into trackPane.children
    trackChoiceController.show()
  }

  override def draw(entities: List[Entity]): Unit = Platform runLater {
    entitiesPane.children.clear()
    entities foreach (entity => Rendering an entity into entitiesPane.children)
  }

  override def animate(entity: Entity): Unit = Platform runLater {
    Animating an entity into animationsPane.children
  }

  override def newTrack(): Unit = {
    trackPane.children.clear()
    Rendering a gameGrid into trackPane.children
  }

  private def setChildren(): Unit = {
    trackChoiceController.setParent(this)
    gameOverController.setParent(this)
  }

  /** Some private verbose methods. */
  private object GameUtilities {

    def setLayouts(): Unit = {
      Rendering.setLayout(gameBoard, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(trackPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(highlightPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(entitiesPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(animationsPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
      trackChoiceController.setLayout()
    }

    def setTransparency(): Unit = {
      entitiesPane.setMouseTransparent(true)
      animationsPane.setMouseTransparent(true)
      highlightPane.setMouseTransparent(true)
      trackChoiceController.setTransparency()
      gameOverController.setTransparency()
      gameMenuController.disableAllButtons()
    }

    def resetAll(): Unit = {
      trackPane.children.clear()
      highlightPane.children.clear()
      entitiesPane.children.clear()
      animationsPane.children.clear()
      trackChoiceController.reset()
      gameOverController.reset()
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

  /** Mouse events handlers. */
  private object MouseEvents {
    import InputEventHandlers._

    def setMouseHandlers(): Unit = {
      trackPane.onMouseExited = _ => removeEffects()
      trackPane.onMouseMoved = MouseEvents.move(_)
      trackPane.onMouseClicked = MouseEvents.click(_)
    }

    def click(e: MouseEvent): Unit = {
      if (!gameMenuController.isPaused && !gameMenuController.anyTowerSelected())
        clickedTower(e, ask, gameMenuController.fillTowerStatus)
      if (!gameMenuController.isPaused && gameMenuController.anyTowerSelected())
        placeTower(e)
    }

    def move(e: MouseEvent): Unit = {
      removeEffects()
      if (!gameMenuController.isPaused && gameMenuController.anyTowerSelected())
        hoverCell(e, ask)
      else {
        e.getTarget.setCursor(Cursor.Default)
      }
    }

    private def placeTower(e: MouseEvent): Unit = {
      val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
      ask(Selectable(cell)).onComplete {
        case Success(value) =>
          value.asInstanceOf[Selected] match {
            case Selected(selectable) =>
              if (selectable) {
                Platform runLater {
                  removeEffects()
                  gameMenuController.unselectDepot()
                  send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
                }
              }
          }
        case Failure(exception) => println(exception)
      }
    }
  }
}
