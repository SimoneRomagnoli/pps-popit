import javafx.scene.layout.BorderPane
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{ FXMLLoader, NoDependencyResolver }
import view.controllers.ViewPodiumController
import scalafx.Includes._

object PodiumTest extends JFXApp3 {

  override def start(): Unit = {
    val loader: FXMLLoader =
      new FXMLLoader(getClass.getResource("/fxml/podium.fxml"), NoDependencyResolver)
    loader.load()

    val podiumPane: BorderPane = loader.getRoot[BorderPane]
    val podiumController: ViewPodiumController = loader.getController[ViewPodiumController]()

    stage = new PrimaryStage() {
      title = "Pop-It!"
      scene = new Scene(podiumPane)
    }
  }
}
