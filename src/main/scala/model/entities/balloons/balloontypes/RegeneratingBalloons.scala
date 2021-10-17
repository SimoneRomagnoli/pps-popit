package model.entities.balloons.balloontypes

import model.entities.Entities
import model.entities.balloons.BalloonLives.Green
import model.entities.balloons.Balloons.{ complex, Balloon }
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration

object RegeneratingBalloons {
  val regenerationTime: Double = 3
  val maxLife: Double = Green.life

  /**
   * Adds to [[Balloon]] the ability to regenerate its life after a certain amount of time.
   */
  trait Regenerating extends Balloon {
    private[this] var timer: Double = regenerationTime

    private def timing(t: Double): Regenerating = {
      this.timer = t
      this
    }

    private def addLife(balloon: Balloon): Balloon = balloon match {
      case BalloonDecoration(b, decorate) => decorate(addLife(b))
      case b => complex(b)
    }

    private def regenerate(dt: Double): Regenerating =
      dt match {
        case t if t <= 0 && maxLife > this.life =>
          addLife(this)
            .asInstanceOf[Regenerating]
            .timing(regenerationTime)
        case t => this.asInstanceOf[Regenerating].timing(t)

      }

    override def update(dt: Double): Regenerating =
      regenerating((this match {
        case BalloonDecoration(b, _) => b.update(dt)
        case b                    => b
      }).asInstanceOf[Balloon])
        .asInstanceOf[Regenerating]
        .regenerate(this.timer - dt)
        .asInstanceOf[Regenerating]

  }

  case class RegeneratingBalloon(override val balloon: Balloon)
      extends BalloonDecoration(balloon)
      with Regenerating {
    override def instance(balloon: Balloon): BalloonDecoration = regenerating(balloon)

    override def pop(bullet: Entities.Entity): Option[Regenerating] =
      balloon.pop(bullet).map(regenerating)
  }

  def regenerating(balloon: Balloon): RegeneratingBalloon =
    RegeneratingBalloon(balloon).following(balloon).asInstanceOf[RegeneratingBalloon]

}
