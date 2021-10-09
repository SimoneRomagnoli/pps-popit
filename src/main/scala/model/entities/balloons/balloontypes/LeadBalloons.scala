package model.entities.balloons.balloontypes

import model.entities.Entities
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.bullets.Bullets.CannonBall

object LeadBalloons {

  /**
   * Adds to a [[Balloon]] the ability to being popped only by [[CannonBall]] s.
   */
  trait Lead extends Balloon { balloon: Balloon =>

    override def pop(bullet: Entities.Entity): Option[Lead] = bullet match {
      case CannonBall(_) => super.pop(bullet).map(lead)
      case _             => Option(this)
    }
  }

  case class LeadBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Lead {
    override def instance(balloon: Balloon): BalloonDecoration = lead(balloon)
  }

  def lead(balloon: Balloon): LeadBalloon = LeadBalloon(balloon)
}
