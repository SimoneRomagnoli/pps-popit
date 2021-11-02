package view.controllers

import controller.Controller.ControllerMessages.{
  BackToMenu,
  RestartGame,
  ResumeGame,
  SaveCurrentTrack
}
import controller.interaction.Messages.{ Input, Message }
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, VBox }
import scalafxml.core.macros.sfxml
import commons.CommonValues.View.{ gameBoardHeight, gameBoardWidth }
import view.render.Rendering

import scala.concurrent.Future

/**
 * Controller of the pause. This controller contains the pause buttons and sets their event
 * handlers.
 */
trait ViewPauseController extends GameControllerChild {
  def isPaused: Boolean
}

/**
 * Controller class bound to the pause fxml.
 */
@sfxml
class PauseController(
    val pause: HBox,
    val pauseVerticalContainer: VBox,
    val pauseContainer: VBox,
    val resume: ToggleButton,
    val saveTrack: ToggleButton,
    val retryTrack: ToggleButton,
    val quit: ToggleButton,
    var parent: ViewGameController,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewPauseController {
  setup()

  override def setLayout(): Unit = {
    Rendering.setLayout(pause, gameBoardWidth, gameBoardHeight)
    pauseVerticalContainer.setAlignment(Pos.Center)
    pauseContainer.setAlignment(Pos.Center)
  }

  override def setTransparency(): Unit = {
    pause.setPickOnBounds(false)
    pause.visible = false
  }

  override def reset(): Unit = {}

  override def setParent(controller: ViewGameController): Unit = parent = controller
  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = pause.visible = true
  override def hide(): Unit = pause.visible = false

  override def isPaused: Boolean = pause.visible.value

  private def setup(): Unit = {
    retryTrack.onMouseClicked = _ => {
      send(RestartGame())
      parent.gameMenuController.clearTowerStatus()
      hide()
    }
    resume.onMouseClicked = _ => {
      send(ResumeGame())
      parent.gameMenuController.enableAllButtons()
      hide()
    }
    quit.onMouseClicked = _ => {
      send(BackToMenu())
      hide()
    }
    saveTrack.onMouseClicked = _ => {
      hide()
      parent.hideGameEntities()
      Platform.runLater(
        send(SaveCurrentTrack(parent.getScenePosition.getX, parent.getScenePosition.getY))
      )
    }
  }

}
