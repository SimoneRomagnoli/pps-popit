package model.entities

import model.Positions.Vector2D
import model.entities.Balloons.{Balloon, complex, simple}
import model.entities.Entities.{Entity, MovementAbility}

import scala.annotation.tailrec
import scala.language.postfixOps

object Balloons {
  /**
   * A [[Balloon]] is an [[Entity]] with the ability to move thanks to [[MovementAbility]].
   */
  sealed trait Balloon extends Entity with MovementAbility {
    @tailrec
    private def retrieve(f: Balloon => Any): Any = this match {
      case Complex(balloon) => balloon retrieve f
      case s => f(s)
    }
    override def position: Vector2D = retrieve(_.position).asInstanceOf[Vector2D]
    override def speed: Vector2D = retrieve(_.speed).asInstanceOf[Vector2D]

    private def change(f: => Balloon): Balloon = this match {
      case Complex(balloon) => complex(balloon change f)
      case _ => f
    }
    override def at(s: Vector2D): Balloon = change(Simple(position, s))
    override def in(p: Vector2D): Balloon = change(Simple(p, speed))
  }

  /**
   * A [[Simple]] balloon can be wrapped my many layers of [[Complex]] balloons,
   * each of which protects the inner ones.
   */
  case class Simple(override val position: Vector2D = (0.0, 0.0),
                    override val speed: Vector2D = (0.0, 0.0)) extends Balloon
  case class Complex(balloon: Balloon) extends Balloon

  def simple(): Balloon = Simple()
  def complex(balloon: Balloon): Balloon = Complex(balloon)

  def pop(b: Balloon): Option[Balloon] = b match {
    case Complex(internal) => Some(internal)
    case _ => None
  }
}

/**
 * Provides a DSL to define new balloons.
 */
object BalloonType {
  sealed trait BalloonType {
    def life: Int
  }

  sealed class BalloonTypeImpl(override val life: Int) extends BalloonType
  case object Red extends BalloonTypeImpl(1)
  case object Blue extends BalloonTypeImpl(2)
  case object Green extends BalloonTypeImpl(3)

  object BalloonType {
    def apply(life: Int): BalloonTypeImpl = new BalloonTypeImpl(life)
    def unapply(b: BalloonType): Option[Int] = Some(b.life)
  }

  implicit class RichBalloonType(b: BalloonTypeImpl) {
    def balloon: Balloon = b match {
      case BalloonType(n) if n > 1 => complex(BalloonType(n - 1) balloon)
      case _ => simple()
    }
  }
}
