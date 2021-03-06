package view.controllers

import controller.interaction.Messages.{ Input, Message }
import scalafx.scene.layout.{ BorderPane, StackPane }
import scalafxml.core.macros.{ nested, sfxml }
import commons.CommonValues
import view.render.Rendering

import scala.concurrent.Future

/**
 * Principal view controller interface. Every controller needs to have a send method and an ask
 * method to interact with the controller.
 */
trait ViewController {
  def setSend(send: Input => Unit): Unit
  def setAsk(reference: Message => Future[Message]): Unit
  def show(): Unit
  def hide(): Unit
}

trait ViewMainController extends ViewController {
  def gameController: ViewGameController
  def menuController: ViewMainMenuController
  def savedTracksController: ViewSavedTracksController
  def settingsController: ViewSettingsController
}

/**
 * Controller class bound to the main fxml.
 */
@sfxml
class MainController(
    val mainPane: StackPane,
    val game: BorderPane,
    val menu: BorderPane,
    val savedTracks: BorderPane,
    val settings: BorderPane,
    @nested[GameController] val gameController: ViewGameController,
    @nested[MainMenuController] val menuController: ViewMainMenuController,
    @nested[SavedTracksController] val savedTracksController: ViewSavedTracksController,
    @nested[SettingsController] val settingsController: ViewSettingsController)
    extends ViewMainController {
  setup()

  override def setSend(reference: Input => Unit): Unit = {
    menuController.setSend(reference)
    gameController.setSend(reference)
    savedTracksController.setSend(reference)
    settingsController.setSend(reference)
  }

  override def setAsk(reference: Message => Future[Message]): Unit = {
    menuController.setAsk(reference)
    gameController.setAsk(reference)
    savedTracksController.setAsk(reference)
    settingsController.setAsk(reference)
  }

  override def show(): Unit = mainPane.visible = true
  override def hide(): Unit = mainPane.visible = false

  private def setup(): Unit = {
    Rendering.setLayout(mainPane, CommonValues.Screen.width, CommonValues.Screen.height)
    gameController.hide()
    savedTracksController.hide()
    settingsController.hide()
    menuController.show()
  }

}
