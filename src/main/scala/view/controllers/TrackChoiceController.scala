package view.controllers

import controller.Controller.ControllerMessages.NewTrack
import controller.interaction.Messages.{ Input, Message }
import scalafx.geometry.Pos
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, VBox }
import scalafxml.core.macros.sfxml
import utils.Commons.View.{ gameBoardHeight, gameBoardWidth }
import view.render.Rendering

import scala.concurrent.Future

/**
 * Controller of the track choice popup. This controller contains the buttons of the popup that is
 * shown when a track can be changed or kept; moreover, sets the event handlers of the buttons.
 */
trait ViewTrackChoiceController extends GameControllerChild

/**
 * Controller class bound to the game-over fxml.
 */
@sfxml
class TrackChoiceController(
    val trackChoice: HBox,
    val trackChoiceVerticalContainer: VBox,
    val trackChoiceContainer: VBox,
    val keepTrack: ToggleButton,
    val changeTrack: ToggleButton,
    var parent: ViewGameController,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewTrackChoiceController {
  import Setters._
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference

  override def show(): Unit = trackChoice.visible = true
  override def hide(): Unit = trackChoice.visible = false

  override def setLayout(): Unit = {
    Rendering.setLayout(trackChoice, gameBoardWidth, gameBoardHeight)
    trackChoiceVerticalContainer.setAlignment(Pos.Center)
    trackChoiceContainer.setAlignment(Pos.Center)
    keepTrack.minWidth = changeTrack.width.value
    keepTrack.maxWidth = changeTrack.width.value
  }

  override def setTransparency(): Unit = {
    trackChoice.setMouseTransparent(false)
    trackChoice.setPickOnBounds(false)
    trackChoice.visible = false
  }
  override def reset(): Unit = trackChoice.children.removeRange(3, trackChoice.children.size)

  override def setParent(controller: ViewGameController): Unit =
    parent = controller

  private object Setters {

    def setup(): Unit =
      setMouseHandlers()

    def setMouseHandlers(): Unit = {
      keepTrack.onMouseClicked = _ => {
        parent.gameMenuController.enableAllButtons()
        hide()
        trackChoice.setMouseTransparent(true)
      }
      changeTrack.onMouseClicked = _ => {
        send(NewTrack())
        parent.newTrack()
        hide()
      }
    }
  }
}
