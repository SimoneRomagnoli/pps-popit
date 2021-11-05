package model.actors

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.ActorRef
import controller.interaction.Messages.Update
import model.actors.BulletActorTest._
import model.actors.BulletMessages.{ BalloonHit, BulletKilled, StartExplosion }
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues.bulletDefaultRadius
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, Explosion }
import model.managers.EntitiesMessages.{ EntityUpdated, UpdateEntity }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

object BulletActorTest {
  var outsideDart: Bullet = Dart() in (Double.MaxValue, 0.0)
  var dart: Bullet = Dart()
  var cannonBall: Explosion = CannonBall(bulletDefaultRadius)
  var balloon: Balloon = Red balloon
  var balloon2: Balloon = Red balloon
  var collisionPosition: (Double, Double) = (100.0, 100.0)

}

class BulletActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers {
  val dartOutsideActor: ActorRef[Update] = testKit.spawn(BulletActor(outsideDart))
  val dartActor: ActorRef[Update] = testKit.spawn(BulletActor(dart))
  val cannonBallActor: ActorRef[Update] = testKit.spawn(BulletActor(cannonBall))
  val model: TestProbe[Update] = testKit.createTestProbe[Update]()

  "The Bullet Actor" when {
    "asked to update" should {
      "update the position of its bullet" in {
        dartActor ! UpdateEntity(0.0, List(dart), model.ref)
        model expectMessage EntityUpdated(dart, dartActor)
      }
    }
    "a bullet touches a balloon" should {
      "hit it" in {
        dart in collisionPosition
        balloon = balloon in collisionPosition
        dartActor ! UpdateEntity(0.0, List(dart, balloon), model.ref)
        model expectMessage BalloonHit(dart, List(balloon))
      }
      "kill the bullet" in {
        model expectMessage BulletKilled(dartActor)
      }
    }
    "a bullet goes out of the screen" should {
      "kill the bullet" in {
        dartOutsideActor ! UpdateEntity(0.0, List(dart), model.ref)
        model expectMessage BulletKilled(dartOutsideActor)
      }
    }
    "an explosion bullet touches a balloon" should {
      "start an explosion " in {
        cannonBall in collisionPosition
        balloon = balloon in collisionPosition
        balloon2 = balloon2 in collisionPosition
        cannonBallActor ! UpdateEntity(0.0, List(cannonBall, balloon, balloon2), model.ref)
        model expectMessage StartExplosion(cannonBall)
      }
      "hit all the balloons in the area" in {
        model expectMessage BalloonHit(cannonBall, List(balloon, balloon2))
      }
      "kill the bullet" in {
        model expectMessage BulletKilled(cannonBallActor)
      }
    }
  }
}
