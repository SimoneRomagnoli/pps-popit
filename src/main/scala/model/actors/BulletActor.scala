package model.actors

import akka.actor.typed.{ scaladsl, ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityKilled, EntityUpdated, Explode, Update, UpdateEntity }
import model.entities.Entities
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, Explosion, IceBall }

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
        //println("bullet actor killed")
        replyTo ! EntityKilled(bullet, ctx.self)
        Behaviors.stopped
      } else {
        entities foreach {
          case balloon: Balloon =>
            if (bullet hit balloon) {
              bullet match {
                case Dart() =>
                  balloon pop bullet
                  replyTo ! EntityKilled(bullet, ctx.self)
                  Behaviors.stopped
                case CannonBall(radius) =>
                  println("it's a cannonball")
                  exploding(entities, bullet.asInstanceOf[Explosion], replyTo)
                case IceBall(radius: Double, freezingTime: Double) =>
                  exploding(entities, bullet.asInstanceOf[Explosion], replyTo)
              }
            }
            Behaviors.same
          case _ =>
        }
        replyTo ! EntityUpdated(bullet)
        Behaviors.same
      }
    case _ => Behaviors.same
  }

  private def exploding(
      entities: List[Entities.Entity],
      explosion: Explosion,
      replyTo: ActorRef[Update]): Behavior[Update] = {
    println("explosion happen")
    entities foreach {
      case balloon: Balloon =>
        if (explosion include balloon) {
          if (explosion.isInstanceOf[CannonBall]) balloon pop explosion
          //else balloon freeze explosion
        }
      case _ =>
    }
    replyTo ! EntityKilled(bullet, ctx.self)
    Behaviors.stopped
  }
}
