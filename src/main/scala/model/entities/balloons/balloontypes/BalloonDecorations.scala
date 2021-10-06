package model.entities.balloons.balloontypes

import model.Positions
import model.entities.Entities
import model.entities.balloons.Balloons.Balloon
import model.maps.Tracks

object BalloonDecorations {

  trait Decoration {
    def balloon: Balloon
  }

  abstract class BalloonDecoration(override val balloon: Balloon) extends Balloon with Decoration {
    override protected[balloons] def retrieve[T](f: Balloon => T): T = balloon.retrieve(f)
    override def life: Int = balloon.life
    override def pop(bullet: Entities.Entity): Option[Balloon] = balloon.pop(bullet)
    override def in(p: Positions.Vector2D): BalloonDecoration = instance(balloon.in(p))
    override def at(s: Positions.Vector2D): BalloonDecoration = instance(balloon.at(s))
    override def on(t: Tracks.Track): BalloonDecoration = instance(balloon.on(t))
    def instance(balloon: Balloon): BalloonDecoration
  }

  object BalloonDecoration {
    def unapply(d: BalloonDecoration): Option[Balloon] = Some(d.balloon)
  }

}
