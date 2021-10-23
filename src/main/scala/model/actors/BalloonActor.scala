package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages.Update
import model.actors.BalloonMessages.{ BalloonKilled, Hit }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.managers.EntitiesMessages.{ EntityUpdated, ExitedBalloon, UpdateEntity }
import utils.Constants

object BalloonMessages {
  case class Hit(bullet: Bullet, replyTo: ActorRef[Update]) extends Update
  case class BalloonKilled(actorRef: ActorRef[Update]) extends Update
}

/**
 * The actor encapsulating a [[Balloon]].
 */
object BalloonActor {

  def apply(balloon: Balloon): Behavior[Update] = Behaviors.setup { ctx =>
    BalloonActor(ctx, balloon).default()
  }
}

/**
 * The [[BalloonActor]] related class, conforming to a common Akka pattern.
 * @param ctx
 *   The actor's context.
 * @param balloon
 *   The encapsulated [[Balloon]].
 */
case class BalloonActor private (
    ctx: ActorContext[Update],
    var balloon: Balloon,
    var hit: Boolean = false) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case UpdateEntity(elapsedTime, _, replyTo) =>
      //if (hit) println("sono stato gia colpito e ora vengo aggiornato")
      balloon.position.x match {
        case outOfBounds if outOfBounds >= Constants.View.gameBoardWidth =>
          replyTo ! ExitedBalloon(balloon, ctx.self)
          Behaviors.stopped
        case _ =>
          balloon = balloon.update(elapsedTime).asInstanceOf[Balloon]
          replyTo ! EntityUpdated(balloon, ctx.self)
          Behaviors.same
      }
    case Hit(bullet, replyTo) =>
      balloon.pop(bullet) match {
        case None =>
          replyTo ! BalloonKilled(ctx.self)
          Behaviors.stopped
        case Some(b) =>
          balloon = b
          //replyTo ! EntityUpdated(balloon, ctx.self)
          Behaviors.same
      }
  }
}
