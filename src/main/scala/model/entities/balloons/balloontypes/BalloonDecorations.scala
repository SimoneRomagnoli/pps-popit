package model.entities.balloons.balloontypes

import model.Positions
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.maps.Tracks

object BalloonDecorations {

  trait Decoration {
    def balloon: Balloon
  }

  /**
   * A generic wrapper for the [[Balloon]] that allows to add the behavior for a
   * [[model.entities.balloons.BalloonDecorations.BalloonType]].
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

    /**
     * @param balloon
     *   The [[Balloon]] to be wrapped with a
     *   [[model.entities.balloons.BalloonDecorations.BalloonType]].
     * @return
     *   The wrapped [[Balloon]].
     */
    def instance(balloon: Balloon): BalloonDecoration
  }

  object BalloonDecoration {

    def unapply(d: BalloonDecoration): Option[(Balloon, Balloon => Balloon)] =
      Some(d.balloon, d.instance)
  }

}
