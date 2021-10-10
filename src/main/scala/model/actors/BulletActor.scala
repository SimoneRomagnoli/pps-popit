package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityUpdated, Update, UpdateEntity }
import model.actors.BulletMessages.{ BalloonHit, BulletKilled }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ Bullet, Explosion }

import scala.language.postfixOps

object BulletMessages {
  case class BalloonHit(bullet: Bullet, balloons: List[Balloon]) extends Update
  case class BulletKilled(actorRef: ActorRef[Update]) extends Update
}

object BulletActor {

  def apply(bullet: Bullet): Behavior[Update] = Behaviors.setup { ctx =>
    BulletActor(ctx, bullet).default()
  }
}

case class BulletActor private (ctx: ActorContext[Update], var bullet: Bullet) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, entities, replyTo) =>
      bullet = bullet.update(elapsedTime).asInstanceOf[Bullet]
      if (bullet.exitedFromScreen()) {
        replyTo ! BulletKilled(ctx.self)
        Behaviors.stopped
      } else {
        val balloons: List[Balloon] = entities.collect { case balloon: Balloon =>
          balloon
        }
        balloons.filter(b => bullet hit b) match {
          case list if list.nonEmpty =>
            exploding(balloons, bullet, replyTo)
          case _ =>
            replyTo ! EntityUpdated(bullet, ctx.self)
            Behaviors.same
        }
      }
    case _ => Behaviors.same
  }

  /**
   * Behaviour that the bulletActor assume when the bullet hits a balloon.
   * @param balloons
   *   all the balloons in the game
   * @param bullet
   *   current bullet
   * @param replyTo
   *   model
   * @return
   *   Stop the behaviour and kill the actor
   */

  private def exploding(
      balloons: List[Balloon],
      bullet: Bullet,
      replyTo: ActorRef[Update]): Behavior[Update] = {
    bullet match {
      case bullet: Explosion =>
        replyTo ! BalloonHit(bullet, balloons.filter(bullet include _))
      case bullet =>
        replyTo ! BalloonHit(bullet, List(balloons.filter(bullet hit _).sorted.reverse.head))
    }
    replyTo ! BulletKilled(ctx.self)
    Behaviors.stopped
  }
}
