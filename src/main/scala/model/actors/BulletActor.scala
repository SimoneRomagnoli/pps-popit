package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityKilled, EntityUpdated, Update, UpdateEntity }
import model.entities.Entities
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletMessages.BalloonHit
import model.entities.bullets.Bullets.{ Bullet, Explosion }

import scala.language.postfixOps

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
        replyTo ! EntityKilled(bullet, ctx.self)
        Behaviors.stopped
      } else {
        entities.collect { case balloon: Balloon =>
          balloon
        }.filter(b => bullet hit b).sorted.reverse match {
          case h :: _ =>
            replyTo ! BalloonHit(bullet, h)
            exploding(entities, bullet, replyTo)
          case _ =>
            replyTo ! EntityUpdated(bullet, ctx.self)
            Behaviors.same
        }
      }
    case _ => Behaviors.same
  }

  /**
   * Behaviour that the bulletActor assume when the bullet hits a balloon.
   * @param entities
   *   all the entities in the game
   * @param bullet
   *   current bullet
   * @param replyTo
   *   model
   * @return
   *   Stop the behaviour and kill the actor
   */

  private def exploding(
      entities: List[Entities.Entity],
      bullet: Bullet,
      replyTo: ActorRef[Update]): Behavior[Update] = {
    bullet match {
      case bullet: Explosion =>
        entities foreach {
          case balloon: Balloon =>
            if (bullet include balloon) {
              replyTo ! BalloonHit(bullet, balloon)
            }
          case _ =>
        }
      case _ =>
    }
    replyTo ! EntityKilled(bullet, ctx.self)
    Behaviors.stopped
  }
}
