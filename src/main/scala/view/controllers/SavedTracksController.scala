package view.controllers

import controller.Controller.ControllerMessages.{ NewGame, RetrieveAndLoadTrack }
import controller.Messages.{ Input, Message }
import javafx.scene.layout.Background
import model.maps.Tracks.Track
import scalafx.application.Platform
import scalafx.scene.control.ToggleButton
import scalafx.scene.image.{ Image, ImageView }
import scalafx.scene.layout._
import scalafxml.core.macros.sfxml
import utils.Constants
import view.render.Drawings.{ Drawing, MenuDrawings }
import view.render.Rendering

import scala.concurrent.Future

trait ViewSavedTracksController extends ViewController {
  def setup(tracks: List[Track]): Unit
}

@sfxml
class SavedTracksController(
    val savedTracksPane: BorderPane,
    val flowPaneTracks: FlowPane,
    val titleLogo: HBox,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewSavedTracksController {
  val drawing: Drawing = Drawing(MenuDrawings())

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = savedTracksPane.visible = true
  override def hide(): Unit = savedTracksPane.visible = false

  override def setup(tracks: List[Track]): Unit = savedTracksSettings.setup(tracks)

  private object savedTracksSettings {

    def setup(tracks: List[Track]): Unit = {
      Rendering.setLayout(savedTracksPane, Constants.Screen.width, Constants.Screen.height)
      Platform.runLater(
        Rendering.forInput(500, 80, "images/backgrounds/SAVED_TRACKS.png") into titleLogo.children
      )
      loadSavedTracks(tracks)
    }

    def loadSavedTracks(tracks: List[Track]): Unit =
      for (i <- tracks.indices) {
        val btn = new ToggleButton("")
        println("Trying to load the " + i + "image")
        val image: ImageView = new ImageView(new Image("images/tracks/track" + i + ".png"))
        image.setFitWidth(Constants.Screen.width / 4.2)
        image.setFitHeight(Constants.Screen.height / 3.2)
        btn.setGraphic(image)
        btn.setId(i.toString)
        btn.onMouseClicked = _ => send(RetrieveAndLoadTrack(btn.getId.toInt))

        btn.styleClass += "savedTrackButton"
        Rendering.setLayout(btn, Constants.Screen.width / 4, Constants.Screen.height / 3)
        Platform.runLater(flowPaneTracks.children += btn)
      }
  }
}
