package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityUpdated, Update, UpdateEntity }
import model.entities.balloons.Balloons.Balloon

object BalloonActor {

  def apply(balloon: Balloon): Behavior[Update] = Behaviors.setup { ctx =>
    BalloonActor(ctx, balloon).default()
  }
}

case class BalloonActor private (
    ctx: ActorContext[Update],
    var balloon: Balloon,
    var linearPosition: Double = 0.0) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, _, replyTo, track) =>
      linearPosition = linearPosition + balloon.speed.x * elapsedTime
      balloon = balloon in (track.exactPosition(
        linearPosition
      ) - (balloon.boundary._1 / 2, balloon.boundary._2 / 2))
      replyTo ! EntityUpdated(balloon)
      Behaviors.same
  }
}
