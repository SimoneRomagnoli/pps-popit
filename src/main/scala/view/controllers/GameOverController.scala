package view.controllers

import controller.Messages.{ Input, Message }
import scalafx.geometry.Pos
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, VBox }
import scalafxml.core.macros.sfxml
import utils.Constants.View.{ gameBoardHeight, gameBoardWidth }
import view.render.Rendering

import scala.concurrent.Future

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
    val saveTrack: ToggleButton,
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

  override def setLayout(): Unit = Rendering.setLayout(gameOver, gameBoardWidth, gameBoardHeight)

  override def setTransparency(): Unit = {
    gameOver.setPickOnBounds(false)
    gameOver.visible = false
  }

  override def reset(): Unit = {}

  private object Setters {

    def setup(): Unit =
      gameOverVerticalContainer.setAlignment(Pos.Center)
    //retryTrack.onMouseClicked = _ => send(Retry())
    //saveTrack.onMouseClicked = _ => send(SaveTrack())
    retryTrack.onMouseClicked = _ => hide()
  }

}
