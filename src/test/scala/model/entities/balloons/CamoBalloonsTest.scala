package model.entities.balloons

import model.entities.balloons.BalloonLives.{ Blue, Red }
import model.entities.balloons.BalloonTypeTest.{
  testChangeValues,
  testDefaultValues,
  testMovement,
  testPoppingByAllBullets,
  testSameStructure
}
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.CamoBalloons.{ camo, CamoBalloon }
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class CamoBalloonsTest extends AnyFlatSpec with Matchers {
  val instance: Balloon => CamoBalloon = camo
  val balloon: CamoBalloon = instance(Red balloon)

  "A camo balloon" should "have default values" in {
    testDefaultValues(balloon)
  }

  it should "be able to change values" in {
    testChangeValues(balloon)
    testSameStructure(instance)
  }

  it should "be able to move" in {
    testMovement(balloon)
  }

  it should "be popped by all the bullets" in {
    testPoppingByAllBullets(instance)
  }

  it should "be able to be double wrap a balloon" in {
    instance(instance(Blue balloon)).pop(Dart()).get shouldBe instance(instance(Red balloon))
    instance(instance(Blue balloon)).pop(IceBall()).get shouldBe instance(instance(Red balloon))
    instance(instance(Blue balloon)).pop(CannonBall()).get.pop(CannonBall()) shouldBe None
  }

}
