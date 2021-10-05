package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.Green
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

}
