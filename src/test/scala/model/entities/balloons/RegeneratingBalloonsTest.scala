package model.entities.balloons

import model.entities.balloons.BalloonLives._
import model.entities.balloons.BalloonTypeTest.{testChangeValues, testDefaultValues, testMovement, testPoppingByAllBullets, testSameStructure, zeroVector}
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.balloons.balloontypes.RegeneratingBalloons.{RegeneratingBalloon, regenerating, regenerationTime}
import model.entities.bullets.Bullets.Dart
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class RegeneratingBalloonsTest extends AnyFlatSpec with Matchers {
  val instance: Balloon => RegeneratingBalloon = regenerating
  val balloon: RegeneratingBalloon = instance(Red balloon)

  "A Regenerating balloon" should "have default values" in {
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

  it should "regenerate its life" in {
    val popped: BalloonDecoration = regenerating(Green balloon).pop(Dart()).get
    popped shouldBe regenerating((Blue balloon) at ((Green balloon) speed))
    (popped.update(1) in zeroVector) shouldBe regenerating(
      (Blue balloon) at ((Green balloon) speed)
    )
    popped.update(1).life shouldBe 2
    (popped.update(regenerationTime / 2) in zeroVector) shouldBe regenerating(
      (Blue balloon) at ((Green balloon) speed)
    )
    (popped
      .update(regenerationTime / 2)
      .update(regenerationTime / 2) in zeroVector) shouldBe regenerating(Green balloon)
  }

  it should "be able to double wrap a balloon" in {
    val popped: BalloonDecoration = regenerating(regenerating(Green balloon)).pop(Dart()).get
    popped shouldBe regenerating(regenerating((Blue balloon) at ((Green balloon) speed)))
    (popped.update(1) in zeroVector) shouldBe regenerating(
      regenerating((Blue balloon) at ((Green balloon) speed))
    )
    popped.update(1).life shouldBe 2
    (popped.update(regenerationTime) in zeroVector) shouldBe regenerating(
      regenerating(Green balloon)
    )
    popped.update(regenerationTime).life shouldBe 3
  }
}
