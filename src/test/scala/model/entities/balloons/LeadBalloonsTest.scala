package model.entities.balloons

import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.entities.balloons.BalloonTypeTest.{
  testChangeValues,
  testDefaultValues,
  testMovement,
  testSameStructure
}
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.LeadBalloons.{ lead, LeadBalloon }
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class LeadBalloonsTest extends AnyFlatSpec with Matchers {
  val instance: Balloon => LeadBalloon = lead
  val balloon: LeadBalloon = instance(Red balloon)

  "A lead balloon" should "have default values" in {
    testDefaultValues(balloon)
  }

  it should "be able to change values" in {
    testChangeValues(balloon)
    testSameStructure(instance)
  }

  it should "be able to move" in {
    testMovement(balloon)
  }

  it should "be popped only by cannon balls" in {
    instance(Green balloon).pop(Dart()).get shouldBe instance(Green balloon)
    instance(Green balloon).pop(IceBall()).get shouldBe instance(Green balloon)
    instance(Green balloon).pop(CannonBall()).get shouldBe instance(Blue balloon)
  }

  it should "be able to be double wrap a balloon" in {
    instance(instance(Blue balloon)).pop(Dart()).get shouldBe instance(instance(Blue balloon))
    instance(instance(Blue balloon)).pop(CannonBall()).get shouldBe instance(instance(Red balloon))
    instance(instance(Blue balloon)).pop(CannonBall()).get.pop(CannonBall()) shouldBe None
  }

}
