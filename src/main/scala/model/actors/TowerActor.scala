package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ CollisionDetected, SearchBalloon, Update }
import model.entities.towers.Towers.CollisionBox

import scala.language.postfixOps

object TowerActor {

  def apply(tower: CollisionBox): Behavior[Update] = Behaviors.setup { ctx =>
    TowerActor(ctx, tower) searching
  }
}

case class TowerActor(ctx: ActorContext[Update], tower: CollisionBox) {

  private def searching: Behavior[Update] = Behaviors.receiveMessage {
    case SearchBalloon(replyTo, position, radius) =>
      if (tower collidesWith (position, radius)) {
        replyTo ! CollisionDetected()
      }
      Behaviors.same
    case _ => Behaviors.same
  }
}
