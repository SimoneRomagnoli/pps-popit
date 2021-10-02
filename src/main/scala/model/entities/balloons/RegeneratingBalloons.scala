package model.entities.balloons

import model.entities.Entities
import model.entities.balloons.BalloonLives.Green
import model.entities.balloons.Balloons.{ complex, Balloon, Complex, Simple }

import scala.language.implicitConversions

object RegeneratingBalloons {
  val regenerationTime: Double = 3
  val maxLife: Double = Green.life

  /**
   * Adds to [[Balloon]] the ability to regenerate its life.
   */
  trait Regenerating extends Balloon { balloon: Balloon =>
    private[this] var timer: Double = regenerationTime

    private def regenerate(dt: Double): Regenerating = timer - dt match {
      case t if t <= 0 && maxLife > this.life =>
        timer = regenerationTime; regenerating(complex(this))
      case t => println(t); timer = t; this
    }

    override def pop(bullet: Entities.Entity): Option[Regenerating] =
      super.pop(bullet).map(regenerating)

    abstract override def update(dt: Double): Regenerating =
      regenerating(super.update(dt).asInstanceOf[Balloon]).regenerate(dt)
  }

  /*class RegeneratingSimple(
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = balloonDefaultSpeed,
      override val boundary: (Double, Double) = balloonDefaultBoundary,
      override val track: Track = Track())
      extends Simple(position, speed, boundary)
      with Regenerating

  class RegeneratingComplex(override val balloon: Balloon)
      extends Complex(balloon)
      with Regenerating*/

  /*def regenerating(b: Balloon): Regenerating = b match {
    case Complex(balloon)   => new RegeneratingComplex(regenerating(balloon))
    case Simple(p, s, b, t) => new RegeneratingSimple(p, s, b, t)
    case _                  => new RegeneratingSimple()
  }*/

  def regenerating(balloon: Balloon): Regenerating = balloon match {
    case Complex(b)         => new Complex(b) with Regenerating
    case Simple(p, s, t, b) => new Simple(p, s, t, b) with Regenerating
    case _                  => new Simple() with Regenerating
  }
}
