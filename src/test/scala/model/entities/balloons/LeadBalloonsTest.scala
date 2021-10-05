package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.{ Green, Red }
import model.entities.balloons.balloontypes.LeadBalloons.lead
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class LeadBalloonsTest extends AnyFlatSpec with Matchers {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)

  "A lead balloon" should "have default position and speed" in {
    lead(Green balloon).position shouldBe zeroVector
    lead(Green balloon).speed shouldBe oneVector
    lead((Green balloon) in oneVector).position shouldBe oneVector
  }

  it should "be able to change speed and position" in {
    (lead(Green balloon) in oneVector).position shouldBe oneVector
    (lead(Green balloon) in oneVector).speed shouldBe oneVector
    (lead(Green balloon) at oneVector).speed shouldBe oneVector
    (lead(Green balloon) at oneVector).position shouldBe zeroVector
  }

  it should "be able to move" in {
    (lead(Red balloon) at zeroVector).update(5.0).position should not be zeroVector
    (lead(Green balloon) at zeroVector).update(5.0).position should not be zeroVector
  }

}
