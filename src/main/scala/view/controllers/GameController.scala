package view.controllers

import controller.interaction.Messages.{ Input, Message }
import javafx.geometry.{ Point2D => Bounds }
import javafx.scene.input.MouseEvent
import model.entities.Entities.Entity
import model.entities.towers.Towers.Tower
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.layout._
import scalafxml.core.macros.{ nested, sfxml }
import utils.Commons
import utils.Commons.Maps.gameGrid
import utils.Commons.View.{ gameBoardHeight, gameBoardWidth, gameMenuHeight, gameMenuWidth }
import view.render.Animations.Animations
import view.render.Drawings.{ Drawing, GameDrawings }
import view.render.{ Animating, Rendering }

import scala.concurrent.Future
import scala.language.{ implicitConversions, reflectiveCalls }

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
  def nextRound(): Unit
  def pauseController: ViewPauseController
  def gameMenuController: ViewGameMenuController
  def gameOverController: ViewGameOverController
  def getScenePosition: Bounds
  def hideGameEntities(): Unit
  def showGameEntities(): Unit
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
    val pause: HBox,
    @nested[TrackChoiceController] val trackChoiceController: ViewTrackChoiceController,
    @nested[PauseController] val pauseController: ViewPauseController,
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
    setTransparencies()
    setMouseHandlers()
    gameMenuController.setup()
    gameMenuController.setHighlightingTower(highlight)
  }

  override def setSend(reference: Input => Unit): Unit = {
    send = reference
    trackChoiceController.setSend(reference)
    pauseController.setSend(reference)
    gameMenuController.setSend(reference)
    gameOverController.setSend(reference)
  }

  override def setAsk(reference: Message => Future[Message]): Unit = {
    ask = reference
    trackChoiceController.setAsk(reference)
    pauseController.setAsk(reference)
    gameMenuController.setAsk(reference)
    gameOverController.setAsk(reference)
  }

  override def show(): Unit = mainPane.visible = true
  override def hide(): Unit = mainPane.visible = false

  override def reset(): Unit = Platform runLater {
    resetAll()
    setTransparencies()
  }

  override def render(stats: GameStats): Unit = Platform runLater {
    gameMenuController renderStats stats
  }

  override def draw(grid: Grid = Commons.Maps.gameGrid): Unit = Platform runLater {
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

  override def getScenePosition: Bounds =
    gameBoard.localToScreen(gameBoard.getLayoutX, gameBoard.getLayoutY)

  override def hideGameEntities(): Unit = {
    entitiesPane.visible = false
    highlightPane.visible = false
  }

  override def showGameEntities(): Unit = {
    entitiesPane.visible = true
    highlightPane.visible = true
  }

  override def nextRound(): Unit = gameMenuController.nextRound()

  private def setChildren(): Unit = {
    trackChoiceController.setParent(this)
    pauseController.setParent(this)
    gameMenuController.setParent(this)
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
      gameOverController.setLayout()
      pauseController.setLayout()
    }

    def setTransparencies(): Unit = {
      entitiesPane.setMouseTransparent(true)
      animationsPane.setMouseTransparent(true)
      highlightPane.setMouseTransparent(true)
      trackChoiceController.setTransparency()
      pauseController.setTransparency()
      gameOverController.setTransparency()
      gameMenuController.disableAllButtons()
    }

    def resetAll(): Unit = {
      trackPane.children.clear()
      highlightPane.children.clear()
      entitiesPane.children.clear()
      animationsPane.children.clear()
      trackChoiceController.reset()
      pauseController.reset()
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
    import ViewControllerUtilities._

    def setMouseHandlers(): Unit = {
      trackPane.onMouseExited = _ => removeEffects()
      trackPane.onMouseMoved = e => if (!pauseController.isPaused) MouseEvents.move(e)
      trackPane.onMouseClicked = e => if (!pauseController.isPaused) MouseEvents.click(e)
    }

    def click(e: MouseEvent): Unit =
      if (towerSelected) {
        removeEffects()
        gameMenuController.unselectDepot()
        placeTower(e, ask, send, gameMenuController.getSelectedTowerType)
      } else {
        clickedTower(e, ask, gameMenuController.fillTowerStatus)
      }

    def move(e: MouseEvent): Unit =
      if (towerSelected)
        hoverCell(e, ask, trackPane)
      else {
        e.getTarget.setCursor(Cursor.Default)
      }

    private def towerSelected: Boolean = gameMenuController.anyTowerSelected()
  }
}
