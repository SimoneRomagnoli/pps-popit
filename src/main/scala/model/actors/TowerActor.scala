package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ BalloonDetected, EntityUpdated, SearchBalloon, Update, UpdateEntity }
import model.entities.towers.Towers.Tower

import scala.language.postfixOps

object TowerActor {

  def apply(tower: Tower): Behavior[Update] = Behaviors.setup { ctx =>
    TowerActor(ctx, tower) searching
  }
}

case class TowerActor(ctx: ActorContext[Update], tower: Tower) {

  private def searching: Behavior[Update] = Behaviors.receiveMessage {
    case SearchBalloon(replyTo, balloon) =>
      if (tower canSee balloon) {
        replyTo ! BalloonDetected()
      }
      Behaviors.same

    case UpdateEntity(elapsedTime, entities, replyTo, track) =>
      replyTo ! EntityUpdated(tower)
      Behaviors.same
    case _ => Behaviors.same
  }
}
