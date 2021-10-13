package view.controllers

import controller.Controller.ControllerMessages.NewGame
import controller.Messages.{ Input, Message }
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ BorderPane, VBox }
import scalafxml.core.macros.sfxml
import utils.Constants
import view.render.Rendering

import scala.concurrent.Future

trait ViewMainMenuController extends ViewController {}

/**
 * Controller class bound to the main menu.
 */
@sfxml
class MainMenuController(
    val mainMenuPane: BorderPane,
    val mainMenuButtons: VBox,
    val newGameBtn: ToggleButton,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewMainMenuController {
  import MenuSetters._
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = mainMenuPane.visible = true
  override def hide(): Unit = mainMenuPane.visible = false

  private object MenuSetters {

    def setup(): Unit = {
      Rendering.setLayout(mainMenuPane, Constants.Screen.width, Constants.Screen.height)
      setupButtons()
    }

    def setupButtons(): Unit =
      newGameBtn.onMouseClicked = _ => send(NewGame())
  }
}
