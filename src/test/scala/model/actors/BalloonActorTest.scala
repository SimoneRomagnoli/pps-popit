package model.actors

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.ActorRef
import controller.interaction.Messages.{ Input, Update }
import model.Positions.defaultPosition
import model.actors.BalloonActorTest.{ outOfBounds, testBalloon }
import model.actors.BalloonMessages.{ BalloonKilled, Hit, Unfreeze }
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues.bulletFreezingTime
import model.entities.bullets.Bullets.{ Dart, IceBall }
import model.managers.EntitiesMessages.{ EntityUpdated, ExitedBalloon, UpdateEntity }
import model.managers.GameDataMessages.Gain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import commons.CommonValues.Game.balloonHitGain

import scala.language.postfixOps

object BalloonActorTest {
  var gameLoop: Option[ActorRef[Input]] = None
  val testBalloon: Balloon = Red balloon
  val outOfBounds: Balloon = (Red balloon) in (Double.MaxValue, 0.0)
}

class BalloonActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers {
  val balloonActor: ActorRef[Update] = testKit.spawn(BalloonActor(testBalloon))
  val complexActor: ActorRef[Update] = testKit.spawn(BalloonActor(Blue balloon))
  val moreComplexActor: ActorRef[Update] = testKit.spawn(BalloonActor(Green balloon))
  val outOfBoundsActor: ActorRef[Update] = testKit.spawn(BalloonActor(outOfBounds))
  val model: TestProbe[Update] = testKit.createTestProbe[Update]()

  "The balloon actor" when {
    "asked to update" should {
      "send to the model the updated entity" in {
        balloonActor ! UpdateEntity(1.0, List(), model.ref)
        model expectMessage EntityUpdated((Red balloon) update 1.0, balloonActor)
      }
      "update the position of its balloon" in {
        (testBalloon position) should be !== defaultPosition
      }
    }
    "the balloon is out of bounds" should {
      "kill the balloon" in {
        outOfBoundsActor ! UpdateEntity(0.0, List(), model.ref)
        model expectMessage ExitedBalloon(outOfBounds, outOfBoundsActor)
      }
    }
    "hit by a bullet" should {
      "send to the model the money gained" in {
        complexActor ! Hit(Dart(), model.ref)
        model expectMessage Gain(balloonHitGain)
      }
      "also kill the balloon if it has no more life remaining" in {
        balloonActor ! Hit(Dart(), model.ref)
        model expectMessage Gain(balloonHitGain)
        model expectMessage BalloonKilled(balloonActor)
      }
      "freeze the balloon if the bullet is ice" in {
        moreComplexActor ! Hit(IceBall(), model.ref)
        model expectMessage Gain(balloonHitGain)
        moreComplexActor ! UpdateEntity(bulletFreezingTime / 2, List(), model.ref)
        model expectMessage EntityUpdated((Blue balloon) at (Green balloon).speed, moreComplexActor)
      }
      "when the balloon is frozen can be hit anyways" in {
        moreComplexActor ! Hit(Dart(), model.ref)
        model expectMessage Gain(balloonHitGain)
      }
      "when the balloon is frozen can be unfrozen" in {
        moreComplexActor ! Unfreeze
        moreComplexActor ! UpdateEntity(1.0, List(), model.ref)
        model expectMessage EntityUpdated(
          ((Red balloon) at (Green balloon).speed) update 1.0,
          moreComplexActor
        )
      }
    }
  }
}
