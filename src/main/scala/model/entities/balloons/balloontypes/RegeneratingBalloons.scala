package model.entities.balloons.balloontypes

import model.Positions
import model.entities.Entities
import model.entities.balloons.BalloonLives.Green
import model.entities.balloons.Balloons.{ complex, Balloon }
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.maps.Tracks

object RegeneratingBalloons {
  val regenerationTime: Double = 3
  val maxLife: Double = Green.life

  /**
   * Adds to [[Balloon]] the ability to regenerate its life.
   */
  trait Regenerating extends Balloon { balloon: Balloon =>
    private[this] var timer: Double = regenerationTime

    protected def regenerate(dt: Double): Regenerating = timer - dt match {
      case t if t <= 0 && maxLife > this.life =>
        timer = regenerationTime
        regenerating(complex(this match {
          case BalloonDecoration(b) => b
          case b                    => b
        }))
      case t => timer = t; this
    }

    abstract override def update(dt: Double): Regenerating = regenerating((this match {
      case BalloonDecoration(b) => b.update(dt)
      case b                    => b
    }).asInstanceOf[Balloon]).regenerate(dt)
  }

  case class RegeneratingBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Regenerating {
    override def in(p: Positions.Vector2D): Regenerating = regenerating(balloon.in(p))
    override def at(s: Positions.Vector2D): Regenerating = regenerating(balloon.at(s))
    override def on(t: Tracks.Track): Regenerating = regenerating(balloon.on(t))

    override def pop(bullet: Entities.Entity): Option[Regenerating] =
      balloon.pop(bullet).map(regenerating)
  }

  def regenerating(balloon: Balloon): RegeneratingBalloon = RegeneratingBalloon(balloon)

}
