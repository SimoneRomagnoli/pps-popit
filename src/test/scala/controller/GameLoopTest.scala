package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.GameLoopTest._
import controller.Messages._
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import org.scalatest.wordspec.AnyWordSpecLike

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
        replyTo ! ModelUpdated(List(), GameStats())
        Behaviors.same
      case _ => Behaviors.same
    }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class GameLoopTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val model: ActorRef[Update] = testKit.spawn(dummyModel(counter))
  val fastModel: ActorRef[Update] = testKit.spawn(dummyModel(fastCounter))
  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val gameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(model, view.ref))
  val fastGameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(fastModel, view.ref))
  val mapView: TestProbe[Render] = testKit.createTestProbe[Render]()
  val mapGameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(model, mapView.ref))

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
        view expectMessage RenderStats(GameStats())
        view expectMessage RenderEntities(List())
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
    "his time ratio gets changed" should {
      "update the model according to the new time ratio" in {
        fastGameLoop ! Start()
        fastGameLoop ! NewTimeRatio(2.0)

        val initialValue: (Double, Double) = (counter.value, fastCounter.value)
        waitSomeTime()
        val finalValue: (Double, Double) = (counter.value, fastCounter.value)

        (finalValue._1 - initialValue._1) should be < (finalValue._2 - initialValue._2)
      }
    }
    "receives the map from the model" should {
      "send it to the view" in {
        val track: Track = Track()
        mapGameLoop ! Start()
        mapGameLoop ! MapCreated(track)
        mapView expectMessage RenderMap(track)
      }
    }
  }
}
