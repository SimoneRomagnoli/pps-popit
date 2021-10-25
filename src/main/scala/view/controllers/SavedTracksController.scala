package view.controllers

import controller.Messages.{ Input, Message }
import javafx.scene.layout.Background
import scalafx.scene.control.{ TitledPane, Toggle, ToggleButton }
import scalafx.scene.image.Image
import scalafx.scene.layout.{
  BackgroundImage,
  BackgroundPosition,
  BackgroundRepeat,
  BackgroundSize,
  BorderPane,
  FlowPane,
  HBox
}
import scalafxml.core.macros.sfxml
import utils.Constants
import view.render.Drawings.{ Drawing, HighScores, MenuDrawings, Title }
import view.render.Rendering

import scala.concurrent.Future

trait ViewSavedTracksController extends ViewController {}

@sfxml
class SavedTracksController(
    val savedTracksPane: BorderPane,
    val flowPaneTracks: FlowPane,
    val titleLogo: HBox,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewSavedTracksController {
  import savedTracksSettings._
  val drawing: Drawing = Drawing(MenuDrawings())
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = savedTracksPane.visible = true
  override def hide(): Unit = savedTracksPane.visible = false

  private object savedTracksSettings {

    def setup(): Unit = {
      Rendering.setLayout(savedTracksPane, Constants.Screen.width, Constants.Screen.height)
      Rendering.forInput(500, 80, "images/backgrounds/SAVED_TRACKS.png") into titleLogo.children
      loadSavedTracks()
    }

    def loadSavedTracks(): Unit =
      for (_ <- 0 to 5) {
        val btn = new ToggleButton("")

        val backgroundImage: BackgroundImage = new BackgroundImage(
          new Image("images/tracks/track.png"),
          BackgroundRepeat.NoRepeat,
          BackgroundRepeat.NoRepeat,
          BackgroundPosition.Default,
          BackgroundSize.Default
        )
        val background: Background = new Background(backgroundImage)

        btn.setBackground(background)
        Rendering.setLayout(btn, Constants.Screen.width / 4, Constants.Screen.height / 3)

        flowPaneTracks.children += btn
      }
  }
}
