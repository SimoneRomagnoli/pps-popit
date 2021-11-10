package model.entities.balloons.balloontypes

import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration

object CamoBalloons {
  /**
   * Wraps a [[Balloon]] with the [[model.entities.balloons.BalloonDecorations.Camo]]
   * [[model.entities.balloons.BalloonDecorations.BalloonType]].
   * @param balloon
   *   The [[Balloon]] wrapped.
   */
  case class CamoBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon) {
    override def instance(balloon: Balloon): BalloonDecoration = camo(balloon)
  }

  def camo(balloon: Balloon): CamoBalloon =
    CamoBalloon(balloon).following(balloon).asInstanceOf[CamoBalloon]

}
