package view.controllers

import controller.Messages.{ Input, Message }
import scalafx.scene.layout.BorderPane
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants
import view.render.Rendering

import scala.concurrent.Future

/**
 * Principal view controller interface. Every controller needs to have a send method and an ask
 * method to interact with the controller.
 */
trait ViewController {
  def setSend(send: Input => Unit): Unit
  def setAsk(reference: Message => Future[Message]): Unit
}

trait ViewMainController extends ViewController {
  def gameController: ViewGameController
}

/**
 * Controller class bound to the main fxml.
 */
@sfxml
class MainController(
    val mainPane: BorderPane,
    val game: BorderPane,
    @nested[GameController] val gameController: ViewGameController)
    extends ViewMainController {
  Rendering.setLayout(mainPane, Constants.Screen.width, Constants.Screen.height)

  override def setSend(reference: Input => Unit): Unit =
    gameController.setSend(reference)

  override def setAsk(reference: Message => Future[Message]): Unit =
    gameController.setAsk(reference)
}
