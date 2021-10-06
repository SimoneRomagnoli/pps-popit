package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonLives.{ Green, Red }
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.CamoBalloons.camo
import model.maps.Cells.Cell
import model.maps.Tracks.{ Track, TrackMap }
import org.scalatest.matchers.must.Matchers.not
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.Constants.Entities.Balloons.{ balloonDefaultBoundary, balloonDefaultSpeed }
import utils.Constants.Entities.defaultPosition

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

  def testMovement(balloon: Balloon): Unit = {
    (balloon at zeroVector).update(5.0).position should not be zeroVector
    (balloon at zeroVector).update(5.0).position should not be zeroVector
  }

}
