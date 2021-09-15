package model.entities

import model.Positions.toVector
import model.entities.BalloonType.{Green, Red}
import model.entities.Balloons.{Balloon, complex, simple}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.{implicitConversions, postfixOps}

class BalloonsTest extends AnyWordSpec with Matchers {
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
        ((Red balloon) position) shouldBe toVector((0.0, 0.0))
        ((Green balloon) position) shouldBe toVector((0.0, 0.0))
      }
    }
    "changing his position" should {
      "be in the specified position" in {
        ((Red balloon) in toVector((1.0, 1.0))).position shouldBe toVector((1.0, 1.0))
        ((Green balloon) in toVector((1.0, 1.0))).position shouldBe toVector((1.0, 1.0))
      }
    }
  }
}
