package model.entities.balloons.balloontypes

import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.bullets.Bullets.{ Bullet, CannonBall }

object LeadBalloons {

  /**
   * Adds to a [[Balloon]] the ability to being popped only by [[CannonBall]] s.
   */
  trait Lead extends Balloon { balloon: Balloon => }

  case class LeadBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Lead {
    override def instance(balloon: Balloon): BalloonDecoration = lead(balloon)

    override def pop(bullet: Bullet): Option[BalloonDecoration] = bullet match {
      case CannonBall(_) => super.pop(bullet)
      case _             => Option(this)
    }
  }

  def lead(balloon: Balloon): LeadBalloon =
    LeadBalloon(balloon).following(balloon).asInstanceOf[LeadBalloon]
}
