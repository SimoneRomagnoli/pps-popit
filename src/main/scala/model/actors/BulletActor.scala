package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.interaction.Messages.Update
import model.actors.BulletMessages.{ BalloonHit, BulletKilled, StartExplosion }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ Bullet, Explosion }
import model.managers.EntitiesMessages.{ EntityUpdated, UpdateEntity }

import scala.language.postfixOps

object BulletMessages {
  case class BalloonHit(bullet: Bullet, balloons: List[Balloon]) extends Update
  case class BulletKilled(actorRef: ActorRef[Update]) extends Update
  case class StartExplosion(bullet: Bullet) extends Update
}

/**
 * The actor encapsulating a [[Bullet]].
 */
object BulletActor {

  def apply(bullet: Bullet): Behavior[Update] = Behaviors.setup { ctx =>
    BulletActor(ctx, bullet).default()
  }
}

/**
 * The [[BulletActor]] related class, conforming to a common Akka pattern.
 * @param ctx
 *   The actor's context.
 * @param bullet
 *   The encapsulated [[Bullet]].
 */
case class BulletActor private (ctx: ActorContext[Update], var bullet: Bullet) {

  def default(): Behavior[Update] = Behaviors.receiveMessagePartial {
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
  }

  /**
   * Behaviour that the bulletActor assumes when the bullet hits a balloon.
   * @param balloons
   *   all the balloons in the game
   * @param bullet
   *   current bullet
   * @param replyTo
   *   model
   * @return
   *   Stop the behavior and kill the actor
   */

  private def exploding(
      balloons: List[Balloon],
      bullet: Bullet,
      replyTo: ActorRef[Update]): Behavior[Update] = {
    bullet match {
      case bullet: Explosion =>
        replyTo ! StartExplosion(bullet)
        replyTo ! BalloonHit(bullet, balloons.filter(bullet include _))
      case bullet =>
        replyTo ! BalloonHit(bullet, List(balloons.filter(bullet hit _).sorted.reverse.head))
    }
    replyTo ! BulletKilled(ctx.self)
    Behaviors.stopped
  }
}
