package model.entities.balloons

import model.Positions.Vector2D
import model.entities.balloons.BalloonType.Green
import model.entities.balloons.Balloons.{ Balloon, Complex, Simple }

object RegeneratingBalloons {
  val regenerationTime: Double = 50
  val maxLife: Double = Green.life

  /**
   * Adds to [[Balloon]] the ability to regenerate its life.
   */
  trait Regenerating extends Balloon {
    private[this] var timer: Double = regenerationTime

    private def regenerate(dt: Double): Regenerating = timer - dt match {
      case t if t <= 0 && maxLife > this.life =>
        timer = regenerationTime; new RegeneratingComplex(this)
      case t => println(t); timer = t; this
    }

    override def update(dt: Double): Regenerating =
      regenerating(super.update(dt).asInstanceOf[Balloon]).regenerate(dt)
  }

  class RegeneratingSimple(
      override val position: Vector2D = (0.0, 0.0),
      override val speed: Vector2D = (0.0, 0.0),
      override val boundary: Double = 1.0)
      extends Simple(position, speed, boundary)
      with Regenerating

  class RegeneratingComplex(override val balloon: Balloon)
      extends Complex(balloon)
      with Regenerating

  def regenerating(b: Balloon): Regenerating = b match {
    case Complex(balloon) => new RegeneratingComplex(regenerating(balloon))
    case Simple(p, s, b)  => new RegeneratingSimple(p, s, b)
    case _                => new RegeneratingSimple()
  }
}
