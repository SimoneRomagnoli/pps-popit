package model.entities.balloons

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, MovementAbility, Poppable, TrackFollowing }
import model.entities.balloons.Balloons._
import model.entities.balloons.Constants.{ defaultBoundary, defaultPosition, defaultSpeed }
import model.maps.Tracks.Track

import scala.annotation.tailrec
import scala.language.postfixOps

object Balloons {

  /**
   * A [[Balloon]] is an [[Entity]] with the ability to move thanks to [[MovementAbility]].
   */
  trait Balloon extends Entity with TrackFollowing with Poppable {
    type Boundary = (Double, Double)

    @tailrec
    private def retrieve(f: Balloon => Any): Any = this match {
      case Complex(balloon) => balloon retrieve f
      case s                => f(s)
    }
    override def position: Vector2D = retrieve(_.position).asInstanceOf[Vector2D]
    override def speed: Vector2D = retrieve(_.speed).asInstanceOf[Vector2D]
    override def boundary: (Double, Double) = retrieve(_.boundary).asInstanceOf[(Double, Double)]
    override def track: Track = retrieve(_.track).asInstanceOf[Track]

    private def change(f: => Balloon): Balloon = this match {
      case Complex(balloon) => complex(balloon change f)
      case _                => f
    }
    override def at(s: Vector2D): Balloon = change(Simple(position, s, track = track))
    override def in(p: Vector2D): Balloon = change(Simple(p, speed, track = track))

    override def on(t: Track): TrackFollowing = change(Simple(position, speed, track = t))

    override def pop(bullet: Entity): Option[Balloon] = this match {
      case Complex(balloon) => Some(balloon)
      case _                => None
    }

    override def life: Int = this match {
      case Complex(balloon) => 1 + balloon.life
      case _                => 1
    }
  }

  /**
   * A [[Simple]] balloon can be wrapped my many layers of [[Complex]] balloons, each of which
   * protects the inner ones.
   */
  case class Simple(
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = defaultSpeed,
      override val boundary: (Double, Double) = defaultBoundary,
      override val track: Track = Track())
      extends Balloon
  case class Complex(balloon: Balloon) extends Balloon

  def simple(): Balloon = Simple()
  def complex(balloon: Balloon): Balloon = Complex(balloon)
}

/**
 * Provides a DSL to define new balloons.
 */
object BalloonType {

  sealed trait BalloonLife {
    def life: Int
  }

  sealed class BalloonLifeImpl(override val life: Int) extends BalloonLife
  case object Red extends BalloonLifeImpl(1)
  case object Blue extends BalloonLifeImpl(2)
  case object Green extends BalloonLifeImpl(3)

  object BalloonLife {
    def apply(life: Int): BalloonLifeImpl = new BalloonLifeImpl(life)
    def unapply(b: BalloonLife): Option[Int] = Some(b.life)
  }

  implicit class RichBalloonType(b: BalloonLifeImpl) {

    def balloon: Balloon = b match {
      case BalloonLife(n) if n > 1 => complex(BalloonLife(n - 1) balloon)
      case _                       => simple()
    }
  }
}

object Constants {
  val defaultPosition: Vector2D = (0.0, 0.0)
  val defaultSpeed: Vector2D = (1.0, 1.0)
  val defaultBoundary: (Double, Double) = (30.0, 40.0)
}
