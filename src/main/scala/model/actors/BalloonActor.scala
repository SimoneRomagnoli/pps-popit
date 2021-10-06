package model.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.{ EntityUpdated, ExitedBalloon, Update, UpdateEntity }
import model.entities.balloons.Balloons.Balloon
import model.maps.Tracks.Directions.{ Direction, DOWN, LEFT, RIGHT, UP }
import utils.Constants

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
      balloon.position.x match {
        case outOfBounds if outOfBounds >= Constants.View.gameBoardWidth =>
          replyTo ! ExitedBalloon(balloon, ctx.self)
          Behaviors.stopped
        case _ =>
          balloon = balloon.update(elapsedTime).asInstanceOf[Balloon]
          replyTo ! EntityUpdated(balloon)
          Behaviors.same
      }
  }
}
