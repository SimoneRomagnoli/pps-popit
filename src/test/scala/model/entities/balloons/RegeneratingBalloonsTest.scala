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
    regenerating(Green balloon).speed shouldBe zeroVector
  }

  it should "be able to change speed and position" in {
    (regenerating(Green balloon) in oneVector).position shouldBe oneVector
    (regenerating(Green balloon) in oneVector).speed shouldBe zeroVector
    (regenerating(Green balloon) at oneVector).speed shouldBe oneVector
    (regenerating(Green balloon) at oneVector).position shouldBe zeroVector
  }

  it should "be able to move" in {
    (regenerating(Red balloon) at (2.0, 2.0)).update(5.0).position shouldBe fromTuple((10.0, 10.0))
    (regenerating(Green balloon) at (2.0, 2.0)).update(5.0).position shouldBe fromTuple(
      (10.0, 10.0)
    )
  }

  it should "regenerate its life" in {
    val popped: Regenerating = regenerating(Green balloon).pop(null).get.asInstanceOf[Regenerating]
    popped shouldBe regenerating(Blue balloon)
    popped.update(1) shouldBe regenerating(Blue balloon)
    popped.update(regenerationTime) shouldBe regenerating(Green balloon)
  }

}
