import javafx.scene.layout.BorderPane
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{ FXMLLoader, NoDependencyResolver }
import scalafx.Includes._
import view.controllers.ViewSavedTracksController

object PodiumTest extends JFXApp3 {

  override def start(): Unit = {
    val loader: FXMLLoader =
      new FXMLLoader(getClass.getResource("/fxml/saved-tracks.fxml"), NoDependencyResolver)
    loader.load()

    val savedTracksPane: BorderPane = loader.getRoot[BorderPane]
    val savedTrackController: ViewSavedTracksController =
      loader.getController[ViewSavedTracksController]()
    savedTrackController.setup(List())

    stage = new PrimaryStage() {
      title = "Pop-It!"
      scene = new Scene(savedTracksPane)
    }
  }
}
