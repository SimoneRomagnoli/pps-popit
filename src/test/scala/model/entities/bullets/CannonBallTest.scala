package model.entities.bullets

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.CannonBall
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.Constants.Entities.Bullets.{
  bulletDefaultBoundary,
  bulletDefaultDamage,
  bulletDefaultRadius,
  bulletDefaultSpeed,
  bulletFreezingTime
}
import utils.Constants.Entities.defaultPosition

import scala.language.postfixOps

class CannonBallTest extends AnyFlatSpec with Matchers {
  val position: Vector2D = defaultPosition
  val speed: Vector2D = bulletDefaultSpeed
  val damage: Double = bulletDefaultDamage
  val radius: Double = bulletDefaultRadius
  val freezingTime: Double = bulletFreezingTime
  val boundary: (Double, Double) = bulletDefaultBoundary

  val cannonBall: CannonBall = CannonBall(radius)
  val balloon1: Balloon = (Red balloon) in (100.0, 100.0)
  val balloon2: Balloon = (Red balloon) in (102.0, 102.0)
  val balloon3: Balloon = (Red balloon) in (98.0, 98.0)

  "A CannonBall explosion" should "include all the balloon in his damage area" in {
    cannonBall in (0.0, 0.0)
    cannonBall include balloon1 shouldBe false
    cannonBall in (100.0, 100.0)
    cannonBall include balloon1 shouldBe true
    cannonBall include balloon2 shouldBe true
    cannonBall include balloon3 shouldBe true
  }
}
