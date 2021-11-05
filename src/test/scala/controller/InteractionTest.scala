package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.AskPattern.{ schedulerFromActorSystem, Askable }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import commons.Futures.retrieve
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages._
import controller.InteractionTest.buildControllerProbes
import controller.interaction.GameLoop.GameLoopMessages.{ CanStartNextRound, Stop }
import controller.interaction.Messages.{ Input, Render, Update, WithReplyTo }
import controller.settings.Settings.Time.Constants._
import controller.settings.Settings.Time.TimeSettings
import controller.settings.Settings.{ Hard, Settings }
import model.entities.towers.TowerTypes.arrow
import model.managers.EntitiesMessages.PlaceTower
import model.managers.GameDynamicsMessages.{ NewMap, WalletQuantity }
import model.maps.Cells.GridCell
import model.stats.Stats
import org.scalatest.wordspec.AnyWordSpecLike
import view.View.ViewMessages.{ RenderSettings, TrackSaved }

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.language.{ implicitConversions, postfixOps }

object InteractionTest {

  /**
   * This should simplify the testing of interactions between [[ActorRef]] s and [[TestProbe]] s.
   * @tparam P,
   *   the type of the test probe that interacts with the actor.
   */
  sealed trait InteractingTestProbe[P] {
    def afterMessage(input: Input): InteractingTestProbe[P]
    def shouldReceive(probeMessage: Option[P]): Unit
  }

  /**
   * This class joins an actor that receives [[Input]] messages and a generic [[TestProbe]].
   *
   * @param testProbe,
   *   the [[TestProbe]] interacting with the actor.
   * @param controller,
   *   the input actor.
   * @tparam P,
   *   the type of the test probe that interacts with the actor.
   */
  case class ControllerProbes[P](testProbe: TestProbe[P], controller: ActorRef[Input])
      extends InteractingTestProbe[P] {

    /** This method just forwards the input message to the controller. */
    override def afterMessage(input: Input): InteractingTestProbe[P] = {
      controller ! input
      this
    }

    /** This method handles the receiving of the expected (or not) message. */
    override def shouldReceive(probeMessage: Option[P]): Unit = probeMessage match {
      case Some(msg) => testProbe expectMessage msg
      case None      => testProbe.expectNoMessage(FiniteDuration(1000, TimeUnit.MILLISECONDS))
    }
  }

  /**
   * Needed for better test syntax: the probes will directly call the [[InteractingTestProbe]] 's
   * methods.
   */
  implicit def buildControllerProbes[P](probe: TestProbe[P])(implicit
      controller: ActorRef[Input]): ControllerProbes[P] =
    ControllerProbes(probe, controller)
}

class InteractionTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  implicit val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  implicit val model: TestProbe[Update] = testKit.createTestProbe[Update]()
  implicit val gameLoop: TestProbe[Input] = testKit.createTestProbe[Input]()

  val controllerActor: Behavior[Input] = Behaviors.setup { ctx =>
    ControllerActor(ctx, view.ref, null, Settings(), Some(model.ref), Some(gameLoop.ref))
      .default()
  }

  implicit var controller: ActorRef[Input] = testKit spawn controllerActor

  "The Controller" when {
    "interacting" should {
      "create a new track" in {
        model afterMessage NewTrack() shouldReceive Some(NewMap(controller, None))
        gameLoop shouldReceive None
        view shouldReceive None
      }
      "interact with the game loop" in {
        gameLoop afterMessage PauseGame() shouldReceive Some(PauseGame())
        gameLoop afterMessage ResumeGame() shouldReceive Some(ResumeGame())
        model shouldReceive None
        view shouldReceive None
      }
      "update settings" in {
        view afterMessage UpdateSettings() shouldReceive Some(RenderSettings(Settings()))
        view afterMessage SetDifficulty(Hard) shouldReceive Some(RenderSettings(Settings(Hard)))
        view afterMessage SetTimeRatio(doubleTimeRatio) shouldReceive Some(
          RenderSettings(Settings(Hard, TimeSettings(timeRatio = doubleTimeRatio)))
        )
        view afterMessage SetFrameRate(lowFrameRate) shouldReceive Some(
          RenderSettings(Settings(Hard, TimeSettings(lowFrameRate, doubleTimeRatio)))
        )
        gameLoop shouldReceive None
        model shouldReceive None
      }
      "send render messages to the view" in {
        view afterMessage TrackSaved() shouldReceive Some(TrackSaved())
        gameLoop shouldReceive None
        model shouldReceive None
      }
      "send updates to the model" in {
        model afterMessage StartNextRound() shouldReceive Some(StartNextRound())
        val placeTower: Input with Update = PlaceTower(GridCell(0, 0), arrow)
        model afterMessage placeTower shouldReceive Some(WithReplyTo(placeTower, controller))
        gameLoop shouldReceive None
        view shouldReceive None
      }
      "start a new game" in {
        view afterMessage BackToMenu() shouldReceive Some(BackToMenu())
        view afterMessage NewGame(None) shouldReceive Some(NewGame(None))
        model shouldReceive Some(Stop())
        gameLoop shouldReceive Some(Stop())
        model shouldReceive None
        gameLoop shouldReceive None
      }
      "respect the interaction pattern" in {
        controller = testKit spawn controllerActor
        retrieve(controller ? (ref => ActorInteraction(ref, WalletQuantity(controller)))) {
          case msg => msg shouldBe CurrentWallet(Stats.startingWallet)
        }
        model expectMessage WithReplyTo(WalletQuantity(controller), controller)
        controller ! CurrentWallet(Stats.startingWallet)
      }
    }
  }

}
