package model.entities.balloons

import model.Positions.{ defaultPosition, Vector2D }
import model.entities.Entities.{ Entity, PoppingAbility, TrackFollowing }
import model.entities.balloons.BalloonValues.{ balloonDefaultBoundary, balloonDefaultSpeed }
import model.entities.bullets.Bullets.Bullet
import model.maps.Tracks.Track
import commons.CommonValues.Screen.cellSize

import scala.language.{ implicitConversions, postfixOps }

object Balloons {

  /**
   * A [[Balloon]] is the main [[Entity]] of the game which has the ability to move on a [[Track]]
   * thanks to [[TrackFollowing]] and to be popped thanks to [[PoppingAbility]].
   */
  trait Balloon extends Entity with TrackFollowing with PoppingAbility {
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

    override def pop(bullet: Bullet): Option[Balloon] = LazyList
      .iterate(Option(this))(_ flatMap {
        case Complex(balloon) => Some(balloon following this)
        case _                => None
      })
      .take(bullet.damage.toInt + 1)
      .last
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
  def complex(balloon: Balloon): Balloon = Complex(balloon).following(balloon)
}

object BalloonValues {
  val balloonDefaultBoundary: (Double, Double) = (cellSize / 3, cellSize / 2)
  val balloonDefaultSpeed: Vector2D = (1.0, 1.0)
}
