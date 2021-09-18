package model.entities.balloons

import model.Positions.{ toVector, Vector2D }
import model.entities.balloons.BalloonType.{ Green, Red }
import model.entities.balloons.Balloons.{ complex, simple, Balloon, Simple }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.{ implicitConversions, postfixOps }

class BalloonsTest extends AnyWordSpec with Matchers {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)

  "A balloon" when {
    "being created" should {
      "use sum types" in {
        assert(simple().isInstanceOf[Balloon])
        assert(complex(simple()).isInstanceOf[Balloon])
      }
      "use its dsl" in {
        (Red balloon) shouldBe simple()
        (Green balloon) shouldBe complex(complex(simple()))
      }
      "have default position" in {
        ((Red balloon) position) shouldBe zeroVector
        ((Green balloon) position) shouldBe zeroVector
      }
      "have default speed" in {
        ((Red balloon) speed) shouldBe zeroVector
        ((Green balloon) speed) shouldBe zeroVector
      }
    }
    "changing his speed" should {
      "have the specified speed" in {
        ((Red balloon) at oneVector).speed shouldBe oneVector
        ((Green balloon) at oneVector).speed shouldBe oneVector
      }
      "maintain the same position" in {
        ((Red balloon) at oneVector).position shouldBe zeroVector
        ((Green balloon) at oneVector).position shouldBe zeroVector
      }
      "not change his structure" in {
        ((Red balloon) at oneVector) shouldBe Simple(zeroVector, oneVector)
        ((Green balloon) at oneVector) shouldBe complex(complex(Simple(zeroVector, oneVector)))
      }
    }
    "changing his position" should {
      "be in the specified position" in {
        ((Red balloon) in oneVector).position shouldBe oneVector
        ((Green balloon) in oneVector).position shouldBe oneVector
      }
      "maintain the same speed" in {
        ((Red balloon) in oneVector).speed shouldBe zeroVector
        ((Green balloon) in oneVector).speed shouldBe zeroVector
      }
      "not change his structure" in {
        ((Red balloon) in oneVector) shouldBe Simple(oneVector, zeroVector)
        ((Green balloon) in oneVector) shouldBe complex(complex(Simple(oneVector, zeroVector)))
      }
    }
    "updating" should {
      "change his position" in {
        ((Red balloon) at toVector((2.0, 2.0))).update(5.0).position shouldBe toVector((10.0, 10.0))
      }
    }
    "popped" should {
      "pop his outer layer" in {
        (Red balloon).pop(null) shouldBe None
        (Green balloon).pop(null) shouldBe Some(complex(simple()))
      }
    }
  }
}
