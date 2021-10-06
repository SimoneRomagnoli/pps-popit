package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.entities.balloons.balloontypes.CamoBalloons.camo
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class CamoBalloonsTest extends AnyFlatSpec with Matchers {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)

  "A camo balloon" should "have default position and speed" in {
    camo(Green balloon).position shouldBe zeroVector
    camo(Green balloon).speed shouldBe oneVector
    camo((Green balloon) in oneVector).position shouldBe oneVector
  }

  it should "be able to change speed and position" in {
    (camo(Green balloon) in oneVector).position shouldBe oneVector
    (camo(Green balloon) in oneVector).speed shouldBe oneVector
    (camo(Green balloon) at oneVector).speed shouldBe oneVector
    (camo(Green balloon) at oneVector).position shouldBe zeroVector
  }

  it should "be able to move" in {
    (camo(Red balloon) at zeroVector).update(5.0).position should not be zeroVector
    (camo(Green balloon) at zeroVector).update(5.0).position should not be zeroVector
  }

  it should "be popped by all the bullets" in {
    camo(Green balloon).pop(Dart()).get shouldBe camo(Blue balloon)
    camo(Green balloon).pop(IceBall()).get shouldBe camo(Blue balloon)
    camo(Green balloon).pop(CannonBall()).get shouldBe camo(Blue balloon)
  }

  it should "be able to be double wrap a balloon" in {
    camo(camo(Blue balloon)).pop(Dart()).get shouldBe camo(camo(Red balloon))
    camo(camo(Blue balloon)).pop(IceBall()).get shouldBe camo(camo(Red balloon))
    camo(camo(Blue balloon)).pop(CannonBall()).get.pop(CannonBall()) shouldBe None
  }

}
