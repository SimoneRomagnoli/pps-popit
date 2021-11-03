package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.AskPattern.{ schedulerFromActorSystem, Askable }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import commons.Futures.retrieve
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages.{ ActorInteraction, CurrentWallet }
import controller.interaction.Messages.{ Input, Render, Update, WithReplyTo }
import controller.settings.Settings.Settings
import model.managers.GameDynamicsMessages.WalletQuantity
import model.stats.Stats
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
    ControllerActor(ctx, view.ref, null, Settings(), Some(model.ref), Some(gameLoop.ref)).default()
  }

  val controller: ActorRef[Input] = testKit spawn controllerActor

  "The Controller" when {
    "just created" should {
      "have no game" in {
        model.expectNoMessage(FiniteDuration(1000, TimeUnit.MILLISECONDS))
        view.expectNoMessage(FiniteDuration(1000, TimeUnit.MILLISECONDS))
      }
    }
    "interacting" should {
      "interact with the model" in {
        retrieve(controller ? (ref => ActorInteraction(ref, WalletQuantity(controller)))) {
          case msg => msg shouldBe CurrentWallet(Stats.startingWallet)
          case _   => fail()
        }
        model expectMessage WithReplyTo(WalletQuantity(controller), controller)
      }
    }
  }

}
