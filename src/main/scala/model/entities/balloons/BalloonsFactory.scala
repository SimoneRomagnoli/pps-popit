package model.entities.balloons

import model.entities.balloons.BalloonDecorations.BalloonType
import model.entities.balloons.Balloons.{ complex, simple, Balloon }
import model.entities.balloons.balloontypes.CamoBalloons.camo
import model.entities.balloons.balloontypes.LeadBalloons.lead
import model.entities.balloons.balloontypes.RegeneratingBalloons.{ regenerating, Regenerating }
import model.spawn.Rounds.BalloonInfo

import scala.language.{ implicitConversions, postfixOps }

object BalloonsFactory {

  implicit class RichBalloon(b: Balloon) {

    /**
     * @param balloonTypes
     *   The [[List]] of [[BalloonType]] s that are gonna wrap the plain [[Balloon]].
     * @return
     *   The [[Balloon]] wrapped by all the [[BalloonType]] s given.
     */
    def adding(balloonTypes: List[BalloonType]): Balloon = balloonTypes.distinct match {
      case h :: t => h.decorate(b) adding t
      case _      => b
    }
  }

}

/**
 * Definition of the different balloon types that are gonna be used by the balloon's DSL.
 */
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

/**
 * Definition of the different balloon lives that are gonna be used by the balloon's DSL.
 */
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

    /**
     * @return
     *   A [[Balloon]] from his life.
     */
    def balloon: Balloon = l match {
      case BalloonLife(n) if n > 1 =>
        complex(BalloonLife(n - 1) balloon) at (simple().speed * (n / 1.5))
      case _ => simple()
    }

    /**
     * @param t
     *   The [[List]] of [[BalloonType]] s that are gonna wrap the [[Balloon]].
     * @return
     *   The complete [[BalloonInfo]] for the [[Balloon]], including [[BalloonLife]] and all the
     *   [[BalloonType]] s.
     */
    def &(t: List[BalloonType]): BalloonInfo = BalloonInfo(t, l)
  }

}
