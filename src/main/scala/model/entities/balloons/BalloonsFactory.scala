package model.entities.balloons

import model.entities.balloons.BalloonDecorations.BalloonType
import model.entities.balloons.Balloons.{ complex, simple, Balloon }
import model.entities.balloons.balloontypes.CamoBalloons.camo
import model.entities.balloons.balloontypes.LeadBalloons.lead
import model.entities.balloons.balloontypes.RegeneratingBalloons.{ regenerating, Regenerating }
import model.spawn.SpawnManager.BalloonInfo

import scala.language.{ implicitConversions, postfixOps }

object BalloonsFactory {

  implicit class RichBalloon(b: Balloon) {

    def adding(balloonTypes: List[BalloonType]): Balloon = balloonTypes match {
      case h :: t => h.decorate(b) adding t
      case _      => b
    }
  }

}

object BalloonDecorations {

  sealed trait BalloonDecorator[T <: Balloon] extends Balloon {
    def decorate(balloon: Balloon): T
  }
  sealed trait BalloonType extends BalloonDecorator[Balloon]

  case object Regenerating extends BalloonType {
    override def decorate(balloon: Balloon): Regenerating = regenerating(balloon)
  }

  case object Camo extends BalloonType {
    override def decorate(balloon: Balloon): Balloon = camo(balloon)
  }

  case object Lead extends BalloonType {
    override def decorate(balloon: Balloon): Balloon = lead(balloon)
  }

  implicit def elementToList(element: BalloonType): List[BalloonType] = List(element)

}

object BalloonLives {

  trait Life {
    def life: Int
  }
  class BalloonLife(override val life: Int) extends Life
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

    def &(t: List[BalloonType]): BalloonInfo = BalloonInfo(t, l)
  }

}
