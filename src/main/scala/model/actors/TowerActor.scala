package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ CollisionDetected, SearchBalloon, Update }
import model.tower.Tower.Tower

import scala.language.postfixOps

object TowerActor {

  def apply(tower: Tower): Behavior[Update] = Behaviors.setup { ctx =>
    TowerActor(ctx, tower) searching
  }
}

case class TowerActor(ctx: ActorContext[Update], tower: Tower) {

  private def searching: Behavior[Update] = Behaviors.receiveMessage {
    case SearchBalloon(replyTo, position, radius) =>
      if (tower collidesWith (position, radius)) {
        replyTo ! CollisionDetected()
      }
      Behaviors.same
    case _ => Behaviors.same
  }
}
