package model.actors

import akka.actor.typed.{ Behavior, PostStop }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityKilled, EntityUpdated, Update, UpdateEntity }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet

import scala.language.postfixOps

object BulletActor {

  def apply(bullet: Bullet): Behavior[Update] = Behaviors.setup { ctx =>
    BulletActor(ctx, bullet).default()
  }
}

case class BulletActor private (ctx: ActorContext[Update], var bullet: Bullet) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, entities, replyTo, _) =>
      bullet = bullet.update(elapsedTime).asInstanceOf[Bullet]
      if (bullet.exitedFromScreen()) {
        println("bullet actor killed")
        replyTo ! EntityKilled(bullet, ctx.self)
        Behaviors.stopped
      } else {
        entities foreach {
          case balloon: Balloon =>
            if (bullet hit balloon) {
              balloon pop bullet
              println("collision detected")
            }
            Behaviors.same
          case _ =>
        }
        replyTo ! EntityUpdated(bullet)
        Behaviors.same
      }
    case _ => Behaviors.same
  }
}
