package model.entities.balloons.balloontypes

import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration

object LeadBalloons {

  trait Lead extends Balloon { balloon: Balloon => }

  case class LeadBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Lead {
    override def instance(balloon: Balloon): BalloonDecoration = lead(balloon)
  }

  def lead(balloon: Balloon): LeadBalloon = LeadBalloon(balloon)
}
