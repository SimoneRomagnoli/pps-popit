package view.controllers

import commons.CommonValues
import controller.Controller.ControllerMessages.{ NewGame, SavedTracksPage, SettingsPage }
import controller.interaction.Messages.{ Input, Message }
import scalafx.scene.control.Button
import scalafx.scene.layout.{ BorderPane, VBox }
import scalafxml.core.macros.sfxml
import view.render.Drawings.{ Drawing, MenuDrawings, Title }
import view.render.Rendering

import scala.concurrent.Future

/**
 * Controller of the main menu. This controller contains the buttons of the main menu and sets their
 * event handlers.
 */
trait ViewMainMenuController extends ViewController {}

/**
 * Controller class bound to the main menu.
 */
@sfxml
class MainMenuController(
    val mainMenuPane: BorderPane,
    val titleLogo: VBox,
    val newGameBtn: Button,
    val savedTracksBtn: Button,
    val settingsBtn: Button,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewMainMenuController {
  import MenuSetters._
  val drawing: Drawing = Drawing(MenuDrawings())
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = mainMenuPane.visible = true
  override def hide(): Unit = mainMenuPane.visible = false

  private object MenuSetters {

    def setup(): Unit = {
      Rendering.setLayout(mainMenuPane, CommonValues.Screen.width, CommonValues.Screen.height)
      val logo = drawing the Title
      Rendering a logo into titleLogo.children
      setupButtons()
    }

    def setupButtons(): Unit = {
      newGameBtn.onMouseClicked = _ => send(NewGame(None))
      savedTracksBtn.onMouseClicked = _ => send(SavedTracksPage())
      settingsBtn.onMouseClicked = _ => send(SettingsPage())
    }
  }
}
