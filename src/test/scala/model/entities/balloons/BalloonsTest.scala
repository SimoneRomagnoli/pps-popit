package model.entities.balloons

import model.Positions.{ fromTuple, Vector2D }
import model.entities.balloons.BalloonLives._
import model.entities.balloons.Balloons.{ complex, simple, Balloon, Simple }
import model.maps.Cells.Cell
import model.maps.Tracks.{ Track, TrackMap }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.Constants.Entities.Balloons.{ balloonDefaultBoundary, balloonDefaultSpeed }
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

class BalloonsTest extends AnyWordSpec with Matchers {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)
  val track: Track = TrackMap(Seq(Cell(0, 0)))

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
        ((Red balloon) position) shouldBe defaultPosition
        ((Green balloon) position) shouldBe defaultPosition
      }
      "have default speed" in {
        ((Red balloon) speed) shouldBe balloonDefaultSpeed
        ((Green balloon) speed) shouldBe balloonDefaultSpeed
      }
      "have default boundary" in {
        ((Red balloon) boundary) shouldBe balloonDefaultBoundary
        ((Green balloon) boundary) shouldBe balloonDefaultBoundary
      }
      "have default track" in {
        ((Red balloon) track) shouldBe Track()
        ((Green balloon) track) shouldBe Track()
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
        ((Red balloon) in oneVector).speed shouldBe oneVector
        ((Green balloon) in oneVector).speed shouldBe oneVector
      }
      "not change his structure" in {
        ((Red balloon) in oneVector) shouldBe Simple(oneVector, oneVector)
        ((Green balloon) in oneVector) shouldBe complex(complex(Simple(oneVector, oneVector)))
      }
    }
    "changing the track" should {
      "be in the specified track" in {
        ((Red balloon) on track).track shouldBe track
        ((Green balloon) on track).track shouldBe track
      }
      "maintain the same speed" in {
        ((Red balloon) on track).speed shouldBe oneVector
        ((Green balloon) on track).speed shouldBe oneVector
      }
      "not change his structure" in {
        ((Red balloon) on track) shouldBe Simple(zeroVector, oneVector, track)
        ((Green balloon) on track) shouldBe complex(complex(Simple(zeroVector, oneVector, track)))
      }
    }
    "updating" should {
      "change his position" in {
        ((Red balloon) at zeroVector).update(5.0).position should not be zeroVector
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
