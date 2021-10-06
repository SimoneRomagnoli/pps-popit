package view.controllers

import akka.actor.typed.ActorRef
import controller.Messages.{ Input, Message }
import scalafx.scene.layout.BorderPane
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants

import scala.concurrent.Future

/**
 * Main controller. This controller loads the main fxml file and contains all the other nested
 * controllers.
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
 *
 * @param mainPane,
 *   the principal container of the application.
 * @param game,
 *   the border pane containing a game.
 * @param gameController,
 *   the view controller of the game.
 */
@sfxml
class MainController(
    val mainPane: BorderPane,
    val game: BorderPane,
    @nested[GameController] val gameController: ViewGameController)
    extends ViewMainController {
  mainPane.maxWidth = Constants.Screen.width
  mainPane.minWidth = Constants.Screen.width
  mainPane.maxHeight = Constants.Screen.height
  mainPane.minHeight = Constants.Screen.height

  override def setSend(reference: Input => Unit): Unit = gameController.setSend(reference)

  override def setAsk(reference: Message => Future[Message]): Unit =
    gameController.setAsk(reference)
}
