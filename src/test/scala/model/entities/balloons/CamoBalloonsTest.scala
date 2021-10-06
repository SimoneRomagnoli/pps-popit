package model.entities.balloons

import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.entities.balloons.BalloonTypeTest.{ testChangeValues, testDefaultValues, testMovement }
import model.entities.balloons.balloontypes.CamoBalloons.{ camo, CamoBalloon }
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class CamoBalloonsTest extends AnyFlatSpec with Matchers {
  val balloon: CamoBalloon = camo(Red balloon)

  "A camo balloon" should "have default values" in {
    testDefaultValues(balloon)
  }

  it should "be able to change values" in {
    testChangeValues(balloon)
  }

  it should "be able to move" in {
    testMovement(balloon)
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
