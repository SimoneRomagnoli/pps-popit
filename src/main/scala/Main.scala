import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.actor.typed.scaladsl.Behaviors
import controller.Controller.ControllerActor
import controller.Messages.{ Input, Message, NewGame, Render }
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import javafx.scene.layout.BorderPane
import scalafxml.core.{ FXMLLoader, NoDependencyResolver }
import view.View.ViewActor
import view.controllers.ViewController

object Main extends JFXApp3 {

  override def start(): Unit = {

    val loader: FXMLLoader =
      new FXMLLoader(getClass.getResource("/fxml/root.fxml"), NoDependencyResolver)

    loader.load()
    val root: BorderPane = loader.getRoot[BorderPane]
    val mainController: ViewController = loader.getController[ViewController]()

    stage = new PrimaryStage() {
      title = "Pop-It!"
      scene = new Scene(root)
    }

    ActorSystem[Message](
      Behaviors.setup[Message] { ctx =>
        val view: ActorRef[Render] = ctx.spawn(ViewActor(mainController), "view")
        val controller: ActorRef[Input] = ctx.spawn(ControllerActor(view), "controller")
        controller ! NewGame()
        Behaviors.empty
      },
      "system"
    )
  }

}
