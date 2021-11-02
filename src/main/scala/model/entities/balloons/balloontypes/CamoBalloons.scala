package model.entities.balloons.balloontypes

import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration

object CamoBalloons {

  /**
   * Adds to a [[Balloon]] the ability to hide from [[model.entities.towers.Towers.Tower]] s.
   */
  trait Camo extends Balloon { balloon: Balloon => }

  /**
   * Wraps a [[Balloon]] with the [[Camo]]
   * [[model.entities.balloons.BalloonDecorations.BalloonType]].
   * @param balloon
   *   The [[Balloon]] wrapped.
   */
  case class CamoBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Camo {
    override def instance(balloon: Balloon): BalloonDecoration = camo(balloon)
  }

  def camo(balloon: Balloon): CamoBalloon =
    CamoBalloon(balloon).following(balloon).asInstanceOf[CamoBalloon]

}
