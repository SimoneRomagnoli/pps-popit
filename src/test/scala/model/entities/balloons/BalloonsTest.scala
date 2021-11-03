package model.entities.balloons

import model.entities.balloons.BalloonLives.{Green, Red}
import model.entities.balloons.BalloonTypeTest.{testChangeValues, testDefaultValues, testMovement, testMultipleDamage, testPoppingByAllBullets, testSameStructure}
import model.entities.balloons.Balloons.{Balloon, complex, simple}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.{implicitConversions, postfixOps}

class BalloonsTest extends AnyWordSpec with Matchers {
  val instance: Balloon => Balloon = b => b
  val balloon: Balloon = instance(Red balloon)

  "A balloon" when {
    "being created" should {
      "use sum types" in {
        assert(simple().isInstanceOf[Balloon])
        assert(complex(simple()).isInstanceOf[Balloon])
      }
      "use its dsl" in {
        (Red balloon) shouldBe simple()
        (Green balloon) shouldBe (complex(complex(simple())) at ((Green balloon) speed))
      }
      "have default values" in {
        testDefaultValues(balloon)
      }
    }
    "changing his values" should {
      "have the specified values" in {
        testChangeValues(balloon)
        testSameStructure(instance)
      }
    }
    "updating" should {
      "change his position" in {
        testMovement(balloon)
      }
    }
    "popped" should {
      "pop his outer layer" in {
        testPoppingByAllBullets(instance)
      }
      "pop according to the bullet's damage" in {
        testMultipleDamage(instance)
      }
    }
  }
}
