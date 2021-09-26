package model.entities.balloons

import model.Positions.{ fromTuple, Vector2D }
import model.entities.balloons.BalloonType.{ Blue, Green, Red }
import model.entities.balloons.RegeneratingBalloons.{ regenerating, regenerationTime, Regenerating }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class RegeneratingBalloonsTest extends AnyFlatSpec with Matchers {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)

  "A Regenerating balloon" should "have default position and speed" in {
    regenerating(Green balloon).position shouldBe zeroVector
    regenerating(Green balloon).speed shouldBe oneVector
  }

  it should "be able to change speed and position" in {
    (regenerating(Green balloon) in oneVector).position shouldBe oneVector
    (regenerating(Green balloon) in oneVector).speed shouldBe oneVector
    (regenerating(Green balloon) at oneVector).speed shouldBe oneVector
    (regenerating(Green balloon) at oneVector).position shouldBe zeroVector
  }

  it should "be able to move" in {
    (regenerating(Red balloon) at zeroVector).update(5.0).position should not be zeroVector
    (regenerating(Green balloon) at zeroVector).update(5.0).position should not be zeroVector
  }

  it should "regenerate its life" in {
    val popped: Regenerating = regenerating(Green balloon).pop(null).get.asInstanceOf[Regenerating]
    popped shouldBe regenerating(Blue balloon)
    (popped.update(1) in zeroVector) shouldBe regenerating(Blue balloon)
    (popped.update(regenerationTime) in zeroVector) shouldBe regenerating(Green balloon)
  }
}
