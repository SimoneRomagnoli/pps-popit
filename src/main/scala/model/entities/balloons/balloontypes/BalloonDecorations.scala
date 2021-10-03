package model.entities.balloons.balloontypes

import model.entities.Entities
import model.entities.balloons.Balloons.Balloon

object BalloonDecorations {

  trait Decoration {
    def balloon: Balloon
  }

  abstract class BalloonDecoration(override val balloon: Balloon) extends Balloon with Decoration {
    override protected[balloons] def retrieve[T](f: Balloon => T): T = balloon.retrieve(f)
    override protected[balloons] def change(f: => Balloon): Balloon = balloon.change(f)
    override def life: Int = balloon.life
    override def pop(bullet: Entities.Entity): Option[Balloon] = balloon.pop(bullet)
  }

  object BalloonDecoration {
    def unapply(d: BalloonDecoration): Option[Balloon] = Some(d.balloon)
  }

}
