import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import commons.CommonValues.Resources.{akkaConfiguration, fxmlRoot}
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages._
import controller.interaction.Messages._
import javafx.scene.layout.StackPane
import scalafx.Includes._
import scalafx.application.{JFXApp3, Platform}
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import view.View.ViewActor
import view.controllers.ViewMainController

import java.io.File
import scala.concurrent.duration.DurationInt

object Main extends JFXApp3 {

  override def start(): Unit = {
    val loader: FXMLLoader = loadRootFXML()
    val root: StackPane = loader.getRoot[StackPane]
    val mainController: ViewMainController = loader.getController[ViewMainController]()

    stage = new PrimaryStage() {
      title = "Pop-It!"
      scene = new Scene(root)
      resizable = false
      onCloseRequest = _ => {
        Platform.exit()
        System.exit(0)
      }
    }

    ActorSystem[Message](
      Behaviors.setup[Message] { ctx =>
        implicit val timeout: Timeout = 3.seconds
        implicit val scheduler: Scheduler = ctx.system.scheduler
        val view: ActorRef[Render] = ctx.spawn(ViewActor(mainController), "view")
        val controller: ActorRef[Input] = ctx.spawn(ControllerActor(view), "controller")
        mainController.setSend(controller ! _)
        mainController.setAsk(request => controller ? (ctx => ActorInteraction(ctx, request)))
        Behaviors.empty
      },
      "system",
      ConfigFactory.load(ConfigFactory.parseFile(new File(akkaConfiguration)))
    )
  }

  private def loadRootFXML(): FXMLLoader = {
    val loader: FXMLLoader =
      new FXMLLoader(getClass.getResource(fxmlRoot), NoDependencyResolver)
    loader.load()
    loader
  }

}
