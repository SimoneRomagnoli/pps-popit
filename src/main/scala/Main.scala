import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, ActorSystem, Scheduler }
import akka.util.Timeout
import controller.Controller.ControllerActor
import controller.Messages._
import javafx.scene.layout.BorderPane
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{ FXMLLoader, NoDependencyResolver }
import view.View.ViewActor
import view.controllers.ViewMainController

import scala.concurrent.duration.DurationInt

object Main extends JFXApp3 {

  override def start(): Unit = {

    val loader: FXMLLoader =
      new FXMLLoader(getClass.getResource("/fxml/root.fxml"), NoDependencyResolver)

    loader.load()
    val root: BorderPane = loader.getRoot[BorderPane]
    val mainController: ViewMainController = loader.getController[ViewMainController]()

    stage = new PrimaryStage() {
      title = "Pop-It!"
      scene = new Scene(root)
    }

    implicit val timeout: Timeout = 3.seconds
    ActorSystem[Message](
      Behaviors.setup[Message] { ctx =>
        implicit val scheduler: Scheduler = ctx.system.scheduler
        val view: ActorRef[Render] = ctx.spawn(ViewActor(mainController), "view")
        val controller: ActorRef[Input] = ctx.spawn(ControllerActor(view), "controller")
        mainController.setSend(controller ! _)
        mainController.setAsk(request => controller ? (ctx => MvcInteraction(ctx, request)))
        controller ! NewGame()
        Behaviors.empty
      },
      "system"
    )
  }

}
