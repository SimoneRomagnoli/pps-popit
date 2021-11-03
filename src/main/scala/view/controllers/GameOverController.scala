package view.controllers

import controller.Controller.ControllerMessages.{ BackToMenu, RestartGame }
import controller.interaction.Messages.{ Input, Message }
import scalafx.geometry.Pos
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, VBox }
import scalafxml.core.macros.sfxml
import commons.CommonValues.View.{ gameBoardHeight, gameBoardWidth }
import view.render.Rendering

import scala.concurrent.Future

/**
 * Controller of the game over popup.
 */
trait ViewGameOverController extends GameControllerChild

/**
 * Controller class bound to the game-over fxml.
 */
@sfxml
class GameOverController(
    val gameOver: HBox,
    val gameOverVerticalContainer: VBox,
    val gameOverContainer: VBox,
    val retryTrack: ToggleButton,
    val quit: ToggleButton,
    var parent: ViewGameController,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewGameOverController {
  import Setters._
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = gameOver.visible = true
  override def hide(): Unit = gameOver.visible = false

  override def setParent(controller: ViewGameController): Unit = parent = controller

  override def setLayout(): Unit = {
    Rendering.setLayout(gameOver, gameBoardWidth, gameBoardHeight)
    gameOverVerticalContainer.setAlignment(Pos.Center)
    gameOverContainer.setAlignment(Pos.Center)
  }

  override def setTransparency(): Unit = {
    gameOver.setPickOnBounds(false)
    gameOver.visible = false
  }

  override def reset(): Unit = {}

  /** Private verbose methods. */
  private object Setters {

    def setup(): Unit = {
      retryTrack.onMouseClicked = _ => {
        send(RestartGame())
        parent.gameMenuController.clearTowerStatus()
        hide()
      }
      quit.onMouseClicked = _ => {
        send(BackToMenu())
        hide()
      }
    }
  }

}
