package model.entities.balloons

import model.entities.balloons.BalloonTypes.{ BalloonType, Plain, Regenerating }
import model.entities.balloons.Balloons.{ complex, simple, Balloon, Simple }
import model.entities.balloons.RegeneratingBalloons.regenerating

import scala.language.postfixOps

object BalloonsFactory {

  /*object Balloon {

    def apply(balloonLife: BalloonLife)(implicit balloonType: BalloonType = Plain): Balloon =
      balloonLife match {
        case BalloonLife(n) if n > 1 => complex(Balloon(BalloonLife(n - 1)))
        case _                       => simple()
      }
  }*/

  implicit class RichBalloon(b: Balloon) {

    def and(balloonType: BalloonType): Balloon = balloonType match {
      case Regenerating => regenerating(b)
      case _            => b
    }
  }

}

/*object BalloonDecorations {

  sealed trait BalloonDecorator
  case object Base extends BalloonDecorator

  case class Decorator[T](balloonDecoration: BalloonDecoration[T], decorator: BalloonDecorator)
      extends BalloonDecorator

  sealed trait BalloonDecoration[T]

  implicit class RichBalloonDecorator(l: BalloonDecorator) {

    def and[T](decoration: BalloonDecoration[T]): BalloonDecorator = Decorator(decoration, l)

    def balloon: Balloon = l match {
      case Decorator(decoration, decorator) => complex(BalloonLife(n - 1) balloon)
      case _                                => simple()
    }
  }

}*/

object BalloonTypes {
  sealed trait BalloonType
  case object Plain extends BalloonType
  case object Regenerating extends BalloonType
}

object BalloonLives {

  trait Life {
    def life: Int
  }
  class BalloonLife(override val life: Int) extends Life // extends BalloonDecoration[BalloonLife]
  case object Red extends BalloonLife(1)
  case object Blue extends BalloonLife(2)
  case object Green extends BalloonLife(3)

  object BalloonLife {
    def apply(life: Int): BalloonLife = new BalloonLife(life)
    def unapply(b: BalloonLife): Option[Int] = Some(b.life)
  }

  implicit class RichBalloonLife(l: BalloonLife) {

    def balloon: Balloon = l match {
      case BalloonLife(n) if n > 1 => complex(BalloonLife(n - 1) balloon)
      case _                       => simple()
    }
  }

}
