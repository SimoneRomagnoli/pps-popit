package model.entities.balloons.balloontypes

import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.bullets.Bullets.{ Bullet, CannonBall }

object LeadBalloons {
  /**
   * Wraps a [[Balloon]] with the [[model.entities.balloons.BalloonDecorations.Lead]]
   * [[model.entities.balloons.BalloonDecorations.BalloonType]].
   * @param balloon
   *   The [[Balloon]] wrapped.
   */
  case class LeadBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon) {
    override def instance(balloon: Balloon): BalloonDecoration = lead(balloon)

    override def pop(bullet: Bullet): Option[BalloonDecoration] = bullet match {
      case CannonBall(_, _) => super.pop(bullet)
      case _                => Option(this)
    }
  }

  def lead(balloon: Balloon): LeadBalloon =
    LeadBalloon(balloon).following(balloon).asInstanceOf[LeadBalloon]
}
