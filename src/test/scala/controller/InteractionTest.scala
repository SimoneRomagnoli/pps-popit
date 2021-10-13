package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.AskPattern.{ schedulerFromActorSystem, Askable }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages.{ ActorInteraction, NewGame }
import controller.GameLoop.GameLoopMessages.Start
import controller.Messages.{ Input, Render, Update }
import model.Model.ModelMessages.WalletQuantity
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object InteractionTest {
  val timeRatio: Double = 1.0
}

class InteractionTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val model: TestProbe[Update] = testKit.createTestProbe[Update]()
  val gameLoop: TestProbe[Input] = testKit.createTestProbe[Input]()

  val controllerActor: Behavior[Input] = Behaviors.setup { ctx =>
    ControllerActor(ctx, view.ref, model.ref, Some(gameLoop.ref)).default()
  }

  var controller: ActorRef[Input] = testKit spawn controllerActor

  "The Controller" when {
    "just created" should {
      "have no game" in {
        model.expectNoMessage(FiniteDuration(1000, TimeUnit.MILLISECONDS))
        view.expectNoMessage(FiniteDuration(1000, TimeUnit.MILLISECONDS))
      }
    }
    "starting a game" should {
      "start a new game loop" in {
        controller ! NewGame()
        gameLoop expectMessage Start()
      }
    }
    "interacting" should {
      "interact with the model" in {
        controller ? (ref => ActorInteraction(ref, WalletQuantity(controller)))
        model expectMessage WalletQuantity(controller)
      }
    }
  }

}
