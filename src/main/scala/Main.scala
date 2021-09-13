import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import controller.Controller.ControllerActor
import controller.Messages.{Input, Message, Render}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import view.View.ViewActor

object Main extends JFXApp {

  val system: ActorSystem[Message] = ActorSystem[Message](Behaviors.setup[Message] { ctx =>
    val view: ActorRef[Render] = ctx.spawn(ViewActor(), "view")
    val controller: ActorRef[Input] = ctx.spawn(ControllerActor(view), "controller")
    //controller ! NewSimulation()
    Behaviors.empty
  }, "system")

  stage = new PrimaryStage {
    title = "Pop-It!"
    scene = new Scene(800, 600) {
      root = ViewActor.board
    }
  }
}
