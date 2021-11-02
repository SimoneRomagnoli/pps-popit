package view.controllers

import controller.Controller.ControllerMessages.{ BackToMenu, RetrieveAndLoadTrack }
import controller.inout.FileCoders.CoderBuilder.trackURL
import controller.interaction.Messages.{ Input, Message }
import scalafx.application.Platform
import scalafx.scene.control.{ ScrollPane, ToggleButton }
import scalafx.scene.image.{ Image, ImageView }
import scalafx.scene.layout._
import scalafxml.core.macros.sfxml
import commons.CommonValues
import view.render.Drawings.{ Drawing, MenuDrawings }
import view.render.Rendering

import scala.concurrent.Future

trait ViewSavedTracksController extends ViewController {
  def setup(numberOfTracks: Int): Unit
}

/**
 * Controller class bound to the saved tracks menu.
 */
@sfxml
class SavedTracksController(
    val savedTracks: BorderPane,
    val scrollPaneTracks: ScrollPane,
    val flowPaneTracks: FlowPane,
    val backToMenu: ToggleButton,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewSavedTracksController {
  val drawing: Drawing = Drawing(MenuDrawings())

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = savedTracks.visible = true
  override def hide(): Unit = savedTracks.visible = false

  override def setup(numberOfTracks: Int): Unit = SavedTracksSettings.setup(numberOfTracks)

  private object SavedTracksSettings {

    def reset(): Unit = flowPaneTracks.children.clear()

    def setup(numberOfTracks: Int): Unit = Platform runLater {
      reset()
      Rendering.setLayout(savedTracks, CommonValues.Screen.width, CommonValues.Screen.height)
      Rendering.setLayout(
        scrollPaneTracks,
        CommonValues.Screen.width * 3 / 4,
        CommonValues.Screen.height * 3 / 4
      )
      backToMenu.onMouseClicked = _ => send(BackToMenu())
      loadSavedTracks(numberOfTracks)
    }

    /**
     * Method for loading from file the previously saved tracks and showing them on the
     * correspondent page.
     * @param numberOfTracks
     *   the number of previously saved tracks
     */
    def loadSavedTracks(numberOfTracks: Int): Unit =
      for (i <- 0 until numberOfTracks) {
        val btn = new ToggleButton("")
        val image: ImageView = new ImageView(new Image(trackURL(i)))
        val btnWidth: Double = CommonValues.Screen.width / 5
        val btnHeight: Double =
          btnWidth * CommonValues.Screen.heightRatio / CommonValues.View.gameBoardWidthRatio
        image.setFitWidth(btnWidth * 0.98)
        image.setFitHeight(btnHeight * 0.98)
        btn.setGraphic(image)
        btn.setId(i.toString)
        btn.onMouseClicked = _ => send(RetrieveAndLoadTrack(btn.getId.toInt))

        btn.styleClass += "savedTrackButton"
        Rendering.setLayout(btn, btnWidth, btnHeight)
        flowPaneTracks.children += btn
      }

  }
}
