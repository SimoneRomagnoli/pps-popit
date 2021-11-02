package controller

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages._
import controller.GameLoopTest._
import controller.interaction.GameLoop.GameLoopActor
import controller.interaction.GameLoop.GameLoopMessages._
import controller.interaction.Messages._
import controller.settings.Settings.Time.TimeSettings
import model.Model.ModelMessages.TickUpdate
import model.entities.bullets.Bullets.Dart
import model.maps.Tracks.Track
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import view.View.ViewMessages._

object GameLoopTest {

  case class Counter(var value: Double = 0.0) {
    def inc(amount: Double): Unit = value = value + amount
  }

  val counter: Counter = Counter()
  val fastCounter: Counter = Counter()

  val dummyModel: Counter => Behavior[Update] = counter =>
    Behaviors.receiveMessage {
      case TickUpdate(elapsedTime, replyTo) =>
        counter.inc(elapsedTime)
        replyTo ! ModelUpdated(List(), List(Dart()))
        Behaviors.same
      case _ => Behaviors.same
    }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class GameLoopTest extends AnyWordSpec with BeforeAndAfterAll with Matchers {
  val testKit: ActorTestKit = ActorTestKit()
  val model: ActorRef[Update] = testKit.spawn(dummyModel(counter))
  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val gameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(model, view.ref, TimeSettings()))
  val mapView: TestProbe[Render] = testKit.createTestProbe[Render]()
  val mapController: ActorRef[Input] = testKit.spawn(ControllerActor(mapView.ref))

  "The GameLoop" when {
    "just created" should {
      "not update the model" in {
        counter.value shouldBe 0.0
        waitSomeTime()
        counter.value shouldBe 0.0
      }
    }
    "started" should {
      "update the model" in {
        counter.value shouldBe 0.0
        gameLoop ! Start()
        waitSomeTime()
        counter.value should be > 0.0
      }
      "order the view to render" in {
        view expectMessage RenderEntities(List())
        view expectMessage StartAnimation(Dart())
      }
    }
    "stopped" should {
      "stop updating the model" in {
        gameLoop ! PauseGame()
        val lastValue: Double = counter.value
        lastValue should be > 0.0
        waitSomeTime()
        counter.value shouldBe lastValue
      }
    }
    "resumed" should {
      "resume updating the model" in {
        val lastValue: Double = counter.value
        gameLoop ! ResumeGame()
        waitSomeTime()
        counter.value should be > lastValue
      }
    }
    "receives the map from the model" should {
      "send it to the view" in {
        val track: Track = Track()
        mapController ! MapCreated(track)
        mapView expectMessage RenderMap(track)
      }
    }
  }
}
