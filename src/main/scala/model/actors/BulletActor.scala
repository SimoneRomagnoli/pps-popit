package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityUpdated, Update, UpdateEntity }
import model.entities.bullets.Bullets.Bullet

object BulletActor {

  def apply(bullet: Bullet): Behavior[Update] = Behaviors.setup { ctx =>
    BulletActor(ctx, bullet).default()
  }
}

case class BulletActor private (ctx: ActorContext[Update], var bullet: Bullet) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, _, replyTo, _) =>
      bullet = bullet.update(elapsedTime).asInstanceOf[Bullet]
      replyTo ! EntityUpdated(bullet)
      Behaviors.same
  }
}
