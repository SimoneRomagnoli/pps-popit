package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{
  BalloonDetected,
  EntitySpawned,
  EntityUpdated,
  SearchBalloon,
  Update,
  UpdateEntity
}
import model.Positions.{ normalized, vector }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import utils.Constants.Entities.Bullets.bulletSpeedFactor

import scala.language.postfixOps

object TowerActor {

  def apply[B <: Bullet](tower: Tower[B]): Behavior[Update] = Behaviors.setup { ctx =>
    TowerActor(ctx, tower) detecting
  }
}

case class TowerActor[B <: Bullet](
    ctx: ActorContext[Update],
    var tower: Tower[B],
    var shootingTime: Double = 0.0) {

  private def detecting: Behavior[Update] = Behaviors.receiveMessage {
    case SearchBalloon(replyTo, balloon) =>
      if (tower canSee balloon) {
        replyTo ! BalloonDetected()
      }
      Behaviors.same

    case UpdateEntity(elapsedTime, entities, replyTo) =>
      entities.collect { case balloon: Balloon =>
        balloon
      }.sorted.findLast(tower canSee _) match {
        case Some(b) =>
          tower = tower rotateTo normalized(vector(tower.position)(b.position))
          if (tower canShootAfter shootingTime) {
            shootingTime = 0.0
            val bullet: Bullet =
              (Bullets shoot tower.bullet) in tower.position at tower.direction * bulletSpeedFactor
            val bulletActor: ActorRef[Update] = ctx.spawnAnonymous(BulletActor(bullet))
            replyTo ! EntitySpawned(bullet, bulletActor)
          }
        case _ =>
      }
      shootingTime += elapsedTime
      replyTo ! EntityUpdated(tower, ctx.self)
      Behaviors.same
    case _ => Behaviors.same
  }
}
