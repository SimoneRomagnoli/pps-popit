package model.entities.balloons

import model.entities.balloons.BalloonLives.Green
import model.entities.balloons.Balloons.{ complex, Balloon, Complex, Simple }

object RegeneratingBalloons {
  val regenerationTime: Double = 3
  val maxLife: Double = Green.life

  /**
   * Adds to [[Balloon]] the ability to regenerate its life.
   */
  trait Regenerating extends Balloon { balloon: Balloon =>
    private[this] var timer: Double = regenerationTime

    def regenerate(dt: Double): Regenerating = timer - dt match {
      case t if t <= 0 && maxLife > this.life =>
        timer = regenerationTime; regenerating(complex(balloon))
      case t => println(t); timer = t; regenerating(balloon)
    }

    override def update(dt: Double): Regenerating =
      regenerating(balloon.update(dt)).regenerate(dt)
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

  def regenerating(b: Balloon): Regenerating = b match {
    case Complex(balloon)   => new Complex(balloon) with Regenerating
    case Simple(p, s, b, t) => new Simple(p, s, b, t) with Regenerating
    case _                  => new Simple() with Regenerating
  }
}
