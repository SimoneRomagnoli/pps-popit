package model.entities.balloons

import model.entities.balloons.BalloonLives.BalloonLife
import model.entities.balloons.BalloonTypes.{ BalloonType, Plain, Regenerating }
import model.entities.balloons.Balloons.{ complex, simple, Balloon }
import model.entities.balloons.RegeneratingBalloons.regenerating

import scala.language.postfixOps

object BalloonsFactory {

  object Balloon {

    def apply(balloonLife: BalloonLife)(implicit balloonType: BalloonType = Plain): Balloon =
      balloonLife match {
        case BalloonLife(n) if n > 1 => complex(Balloon(BalloonLife(n - 1)))
        case _                       => simple()
      }
  }

  implicit class RichBalloon(b: Balloon) {

    def and(balloonType: BalloonType): Balloon = balloonType match {
      case Regenerating => regenerating(b)
      case _            => b
    }
  }

}

object BalloonTypes {
  sealed trait BalloonType
  case object Plain extends BalloonType
  case object Regenerating extends BalloonType
}

object BalloonLives {

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

  implicit class RichBalloonLife(l: BalloonLife) {

    def balloon: Balloon = l match {
      case BalloonLife(n) if n > 1 => complex(BalloonLife(n - 1) balloon)
      case _                       => simple()
    }
  }

}
