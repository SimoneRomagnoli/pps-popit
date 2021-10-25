package model.entities.balloons.balloontypes

import model.Positions
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.CamoBalloons.{ camo, CamoBalloon }
import model.entities.balloons.balloontypes.LeadBalloons.{ lead, LeadBalloon }
import model.entities.balloons.balloontypes.RegeneratingBalloons.{
  regenerating,
  RegeneratingBalloon
}
import model.entities.bullets.Bullets.Bullet
import model.maps.Tracks

object BalloonDecorations {

  trait Decoration {
    def balloon: Balloon
  }

  /**
   * There are different [[model.entities.balloons.BalloonDecorations.BalloonType]] s, each of which
   * can be seen as a decoration of a normal balloon.
   */
  abstract class BalloonDecoration(override val balloon: Balloon) extends Balloon with Decoration {
    override protected[balloons] def retrieve[T](f: Balloon => T): T = balloon.retrieve(f)
    override def life: Int = balloon.life

    override def pop(bullet: Bullet): Option[BalloonDecoration] =
      balloon.pop(bullet).map(instance)
    override def in(p: Positions.Vector2D): BalloonDecoration = instance(balloon.in(p))
    override def at(s: Positions.Vector2D): BalloonDecoration = instance(balloon.at(s))
    override def on(t: Tracks.Track): BalloonDecoration = instance(balloon.on(t))
    override def update(dt: Double): Balloon = instance(balloon.update(dt).asInstanceOf[Balloon])

    def instance(balloon: Balloon): BalloonDecoration
  }

  object BalloonDecoration {

    def unapply(d: BalloonDecoration): Option[(Balloon, Balloon => Balloon)] = Some(
      d.balloon,
      d match {
        case _: RegeneratingBalloon => regenerating
        case _: CamoBalloon         => camo
        case _: LeadBalloon         => lead
      }
    )
  }

}
