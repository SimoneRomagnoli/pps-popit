package model.entities.balloons

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, Poppable, TrackFollowing }
import model.maps.Tracks.Track
import utils.Constants.Entities.Balloons.{ balloonDefaultBoundary, balloonDefaultSpeed }
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Balloons {

  /**
   * A [[Balloon]] is an [[Entity]] with the ability to move on a [[Track]] thanks to
   * [[TrackFollowing]] and to be popped thanks to [[Poppable]].
   */
  trait Balloon extends Entity with TrackFollowing with Poppable {
    type Boundary = (Double, Double)

    protected[balloons] def retrieve[T](f: Balloon => T): T = this match {
      case Complex(balloon) => balloon retrieve f
      case s                => f(s)
    }
    override def position: Vector2D = retrieve(_.position)
    override def speed: Vector2D = retrieve(_.speed)
    override def track: Track = retrieve(_.track)
    override def boundary: (Double, Double) = retrieve(_.boundary)

    override def life: Int = this match {
      case Complex(balloon) => 1 + balloon.life
      case _                => 1
    }

    protected[balloons] def change(f: => Balloon): Balloon = this match {
      case Complex(balloon) => complex(balloon change f)
      case _                => f
    }
    override def in(p: Vector2D): Balloon = change(Simple(p, speed, track))
    override def at(s: Vector2D): Balloon = change(Simple(position, s, track))
    override def on(t: Track): Balloon = change(Simple(position, speed, t))

    override def pop(bullet: Entity): Option[Balloon] = this match {
      case Complex(balloon) => Some(balloon)
      case _                => None
    }
  }

  /**
   * A [[Simple]] balloon can be wrapped by many layers of [[Complex]] balloons, each of which
   * protects the inner ones.
   */
  case class Simple(
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = balloonDefaultSpeed,
      override val track: Track = Track(),
      override val boundary: (Double, Double) = balloonDefaultBoundary)
      extends Balloon
  case class Complex(balloon: Balloon) extends Balloon

  def simple(): Balloon = Simple()
  def complex(balloon: Balloon): Balloon = Complex(balloon)
}
