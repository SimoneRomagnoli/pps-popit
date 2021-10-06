package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.{ Blue, Red }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.maps.Cells.Cell
import model.maps.Tracks.{ Track, TrackMap }
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.Constants.Entities.Balloons.{ balloonDefaultBoundary, balloonDefaultSpeed }
import utils.Constants.Entities.defaultPosition

import scala.language.postfixOps

object BalloonTypeTest {
  val zeroVector: Vector2D = (0.0, 0.0)
  val oneVector: Vector2D = (1.0, 1.0)
  val track: Track = Track()
  val otherTrack: Track = TrackMap(Seq(Cell(0, 0)))

  def testDefaultValues(balloon: Balloon): Unit = {
    balloon.position shouldBe defaultPosition
    balloon.speed shouldBe balloonDefaultSpeed
    balloon.track shouldBe track
    balloon.boundary shouldBe balloonDefaultBoundary
  }

  def testChangeValues(balloon: Balloon): Unit = {
    (balloon in oneVector).position shouldBe oneVector
    (balloon at zeroVector).speed shouldBe zeroVector
    (balloon on otherTrack).track shouldBe otherTrack
  }

  def testSameStructure(instance: Balloon => Balloon): Unit = {
    (instance(Red balloon) in oneVector) shouldBe instance((Red balloon) in oneVector)
    (instance(Red balloon) at zeroVector) shouldBe instance((Red balloon) at zeroVector)
    (instance(Red balloon) on otherTrack) shouldBe instance((Red balloon) on otherTrack)
    (instance(Blue balloon) in oneVector) shouldBe instance((Blue balloon) in oneVector)
    (instance(Blue balloon) at zeroVector) shouldBe instance((Blue balloon) at zeroVector)
    (instance(Blue balloon) on otherTrack) shouldBe instance((Blue balloon) on otherTrack)
  }

  def testMovement(balloon: Balloon): Unit = {
    (balloon at zeroVector).update(5.0).position shouldBe track.exactPositionFrom(0)
    (balloon at oneVector).update(5.0).position.x should be > track.exactPositionFrom(0).x
  }

  def testPoppingByAllBullets(instance: Balloon => Balloon): Unit = {
    instance(Blue balloon).pop(Dart()).get shouldBe instance(Red balloon)
    instance(Blue balloon).pop(IceBall()).get shouldBe instance(Red balloon)
    instance(Blue balloon).pop(CannonBall()).get shouldBe instance(Red balloon)
    instance(Red balloon).pop(Dart()) shouldBe None
    instance(Red balloon).pop(IceBall()) shouldBe None
    instance(Red balloon).pop(CannonBall()) shouldBe None
  }

}
