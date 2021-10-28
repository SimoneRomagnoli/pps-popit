package view.controllers

import controller.Controller.ControllerMessages.{ NewGame, SavedTracksPage, SettingsPage }
import controller.interaction.Messages.{ Input, Message }
import scalafx.geometry.Pos
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ BorderPane, HBox, VBox }
import scalafxml.core.macros.sfxml
import utils.Commons
import view.render.Drawings.{ Drawing, MenuDrawings, Title }
import view.render.Rendering

import scala.concurrent.Future

trait ViewMainMenuController extends ViewController {}

/**
 * Controller class bound to the main menu.
 */
@sfxml
class MainMenuController(
    val mainMenuPane: BorderPane,
    val titleLogo: HBox,
    val mainMenuButtons: VBox,
    val newGameBtn: ToggleButton,
    val savedTracksBtn: ToggleButton,
    val settingsBtn: ToggleButton,
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
      Rendering.setLayout(mainMenuPane, Commons.Screen.width, Commons.Screen.height)
      mainMenuButtons.setAlignment(Pos.Center)

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
