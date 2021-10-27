package model.entities.bullets

import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues._
import model.entities.bullets.Bullets.CannonBall
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class CannonBallTest extends AnyFlatSpec with Matchers {
  val cannonBall: CannonBall = CannonBall(bulletDefaultRadius)
  val balloon1: Balloon = (Red balloon) in (100.0, 100.0)
  val balloon2: Balloon = (Red balloon) in (102.0, 102.0)
  val balloon3: Balloon = (Red balloon) in (98.0, 98.0)

  "A CannonBall explosion" should "include all the balloons in his damage area " +
    "and not the once outside the radius" in {

      cannonBall in (0.0, 0.0)
      cannonBall include balloon1 shouldBe false
      cannonBall in (100.0, 100.0)
      cannonBall include balloon1 shouldBe true
      cannonBall include balloon2 shouldBe true
      cannonBall include balloon3 shouldBe true
    }
}
