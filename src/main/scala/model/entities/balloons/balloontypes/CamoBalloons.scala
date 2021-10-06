package model.entities.balloons.balloontypes

import model.entities.Entities
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration

object CamoBalloons {

  trait Camo extends Balloon { balloon: Balloon => }

  case class CamoBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Camo {
    override def instance(balloon: Balloon): BalloonDecoration = camo(balloon)

    override def pop(bullet: Entities.Entity): Option[Balloon] = balloon.pop(bullet).map(camo)
  }

  def camo(balloon: Balloon): CamoBalloon = CamoBalloon(balloon)

}
