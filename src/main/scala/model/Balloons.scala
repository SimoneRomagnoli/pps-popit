package model

import model.BalloonType.{Blue, Green, Red}
import model.Balloons.{Balloon, Complex, Simple}

import scala.language.postfixOps

object Balloons {
  sealed trait Balloon
  case object Simple extends Balloon
  case class Complex(balloon: Balloon) extends Balloon

  def pop(b: Balloon): Option[Balloon] = b match {
    case Complex(internal) => Some(internal)
    case _ => None
  }
}

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
      case BalloonType(n) if n > 1 => Complex(BalloonType(n - 1) balloon)
      case _ => Simple
    }
  }
}

object Main extends App {
  assert((Red balloon).equals(Simple))
  assert((Blue balloon).equals(Complex(Simple)))
  assert((Green balloon).equals(Complex(Complex(Simple))))
}
