package view.controllers

import cats.effect.IO
import controller.Controller.ControllerMessages.{ NewTrack, PlaceTower }
import controller.Messages.{ Input, Message }
import javafx.scene.input.MouseEvent
import model.entities.Entities.Entity
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.{ Selectable, Selected, TowerIn, TowerOption }
import model.maps.Cells.Cell
import model.maps.Grids.Grid
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Cursor
import scalafx.scene.control.{ Label, ToggleButton }
import scalafx.scene.layout.{ BorderPane, HBox, Pane, StackPane, VBox }
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
  def animate(entity: Entity): Unit
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
    val trackChoiceVerticalContainer: VBox,
    val trackChoiceContainer: HBox,
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
    loading()
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

  override def loading(): Unit = Platform runLater {
    val loadingLabel: Label =
      Label(Constants.View.loadingLabels(Random.between(0, Constants.View.loadingLabels.size)))
    loadingLabel
      .layoutXProperty()
      .bind(gameBoard.widthProperty().subtract(loadingLabel.widthProperty()).divide(2))
    loadingLabel
      .layoutYProperty()
      .bind(gameBoard.heightProperty().subtract(loadingLabel.heightProperty()).divide(2))

    trackPane.children.add(loadingLabel)
  }

  override def reset(): Unit = Platform runLater {
    resetAll()
  }

  override def update(stats: GameStats): Unit = Platform runLater {
    gameMenuController updateStats stats
  }

  override def draw(grid: Grid = Constants.Maps.gameGrid): Unit = Platform runLater {
    Rendering a grid into trackPane.children
  }

  override def draw(track: Track): Unit = Platform runLater {
    occupiedCells = track.cells
    Rendering a track into trackPane.children

    //gameMenuController.enableRoundButton()
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
      Rendering.setLayout(highlightPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(entitiesPane, gameBoardWidth, gameBoardHeight)
      Rendering.setLayout(animationsPane, gameBoardWidth, gameBoardHeight)
      highlightPane.setMouseTransparent(true)
      entitiesPane.setMouseTransparent(true)
      animationsPane.setMouseTransparent(true)
      Rendering.setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
      trackChoiceVerticalContainer.setAlignment(Pos.Center)
    }

    def resetAll(): Unit = {
      trackPane.children.clear()
      highlightPane.children.clear()
      entitiesPane.children.clear()
      animationsPane.children.clear()
    }

    def highlight(tower: Tower[_], insertion: Boolean): Unit =
      if (insertion) {
        Rendering sightOf tower into highlightPane.children
      } else {
        highlightPane.children.clear()
      }

    def removeEffects(): Unit =
      trackPane.children.foreach(_.setEffect(null))
  }

  private object MouseEvents {
    import InputEventHandlers._

    def setMouseHandlers(): Unit = {
      trackPane.onMouseExited = _ => removeEffects()
      trackPane.onMouseMoved = MouseEvents.move(_).unsafeRunSync()
      trackPane.onMouseClicked = MouseEvents.click(_).unsafeRunSync()
      keepTrack.onMouseClicked = _ => {
        trackChoiceContainer.visible = false
        gameMenuController.enableRoundButton()
      }
      changeTrack.onMouseClicked = _ => send(NewTrack())
      trackChoiceContainer.visible = false
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
                  gameMenuController.unselectDepot()
                  send(PlaceTower(cell, gameMenuController.getSelectedTowerType))
                  ask(TowerIn(cell)) onComplete {
                    case Failure(exception) => println(exception)
                    case Success(value) =>
                      value match {
                        case TowerOption(option) =>
                          option match {
                            case Some(_) => occupy(cell)
                            case _       =>
                          }
                        case _ =>
                      }
                  }
                }
              } else {}
          }
        case Failure(exception) => println(exception)
      }
    }

    private def occupy(cell: Cell): Unit =
      occupiedCells = occupiedCells :+ cell
  }
}
