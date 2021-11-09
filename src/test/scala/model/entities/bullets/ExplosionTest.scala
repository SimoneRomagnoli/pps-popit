package model.entities.bullets

import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues._
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Explosion, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class ExplosionTest extends AnyFlatSpec with Matchers {
  var cannonBall: Explosion = CannonBall()
  var iceBall: Explosion = IceBall()
  val balloon1: Balloon = (Red balloon) in (100.0, 100.0)
  val balloon2: Balloon = (Red balloon) in (102.0, 102.0)
  val balloon3: Balloon = (Red balloon) in (98.0, 98.0)

  "A CannonBall explosion" should "include all the balloons in his damage area " +
    "and not the ones outside its radius" in {

      cannonBall = (cannonBall in (0.0, 0.0)).asInstanceOf[Explosion]
      cannonBall isInSightRange balloon1 shouldBe false
      cannonBall = (cannonBall in (100.0, 100.0)).asInstanceOf[Explosion]
      cannonBall isInSightRange balloon1 shouldBe true
      cannonBall isInSightRange balloon2 shouldBe true
      cannonBall isInSightRange balloon3 shouldBe true
    }

  "An IceBall explosion" should "include all the balloons in his damage area " +
    "and not the ones outside its radius" in {

      iceBall = (iceBall in (0.0, 0.0)).asInstanceOf[Explosion]
      iceBall isInSightRange balloon1 shouldBe false
      iceBall = (iceBall in (100.0, 100.0)).asInstanceOf[Explosion]
      iceBall isInSightRange balloon1 shouldBe true
      iceBall isInSightRange balloon2 shouldBe true
      iceBall isInSightRange balloon3 shouldBe true
    }
}
