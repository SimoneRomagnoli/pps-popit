package model.actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{
  BalloonDetected,
  BalloonMoved,
  SearchBalloon,
  Tick,
  Update,
  UpdatePosition
}
import model.Positions.Vector2D
import model.actors.TowerActorTest.{
  balloonDetected,
  balloonPosition,
  dummyBalloonActor,
  dummyModel,
  towerPosition,
  waitSomeTime
}
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.towers.Towers.TowerType.Base
import model.entities.towers.Towers.Tower
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.{ defaultShotRatio, defaultSightRange }
import scala.language.postfixOps

object TowerActorTest {

  val balloonPosition: Vector2D = (60.0, 80.0)
  val towerPosition: Vector2D = (20.0, 10.0)
  var balloonDetected: Boolean = false

  val dummyBalloonActor: Balloon => Behavior[Update] = b =>
    Behaviors.receiveMessage {
      case UpdatePosition(replyTo) =>
        val newBalloon: Balloon = b in ((b.position.x - 20, b.position.y - 30))
        replyTo ! BalloonMoved(newBalloon)
        dummyBalloonActor(newBalloon)
      case _ => Behaviors.same
    }

  val dummyModel: ActorRef[Update] => Behavior[Update] = t =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Tick(replyTo) =>
          balloonDetected = false
          replyTo ! UpdatePosition(ctx.self)
          Behaviors.same
        case BalloonMoved(entity) =>
          t ! SearchBalloon(ctx.self, entity)
          Behaviors.same
        case BalloonDetected() =>
          balloonDetected = true
          Behaviors.same
        case _ => Behaviors.same
      }
    }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class TowerActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val tower: Tower =
    (Base tower) in towerPosition withSightRangeOf defaultSightRange withShotRatioOf defaultShotRatio

  val towerActor: ActorRef[Update] =
    testKit.spawn(TowerActor(tower))
  val model: ActorRef[Update] = testKit.spawn(dummyModel(towerActor))

  val balloonActor: ActorRef[Update] =
    testKit.spawn(dummyBalloonActor(Simple(balloonPosition)))

  "The tower actor" when {
    "has just spawned, it" should {
      "not see the balloon" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
    "the balloon moves through the map, it" should {
      "not see the balloon immediately, but after the second move" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe true
      }
    }
    "the balloon goes far away, it" should {
      "not see the balloon any more" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
  }
}
