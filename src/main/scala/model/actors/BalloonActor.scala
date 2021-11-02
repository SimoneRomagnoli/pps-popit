package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import model.actors.BalloonMessages.{ BalloonKilled, Hit, Unfreeze }
import controller.interaction.Messages.{ EntitiesManagerMessage, Update }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ Bullet, Ice }
import model.managers.EntitiesMessages.{ EntityUpdated, ExitedBalloon, UpdateEntity }
import model.managers.GameDynamicsMessages.Gain
import commons.CommonValues
import commons.CommonValues.Game.balloonHitGain

import scala.concurrent.duration.DurationDouble

object BalloonMessages {
  case class Hit(bullet: Bullet, replyTo: ActorRef[Update]) extends Update
  case class BalloonKilled(actorRef: ActorRef[Update]) extends Update with EntitiesManagerMessage
  case object Unfreeze extends Update
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
case class BalloonActor private (ctx: ActorContext[Update], var balloon: Balloon) {

  /**
   * Default behavior of the [[BalloonActor]]: it waits for an [[UpdateEntity]] message to update
   * itself and can be hit by a [[Bullet]].
   */
  def default(): Behavior[Update] = Behaviors.receiveMessagePartial {
    case UpdateEntity(elapsedTime, _, replyTo) =>
      balloon.position.x match {
        case outOfBounds if outOfBounds >= CommonValues.View.gameBoardWidth =>
          replyTo ! ExitedBalloon(balloon, ctx.self)
          Behaviors.stopped
        case _ =>
          balloon = balloon.update(elapsedTime).asInstanceOf[Balloon]
          replyTo ! EntityUpdated(balloon, ctx.self)
          Behaviors.same
      }

    case Hit(bullet, replyTo) =>
      hit(bullet, replyTo) {
        case ice: Ice => freeze(ice.freezingTime)
        case _        => Behaviors.same
      }

  }

  /**
   * When hit by an [[Ice]] [[Bullet]], the [[BalloonActor]] starts a timer which represent how long
   * it's gonna be frozen for.
   */
  def freeze(freezingTime: Double): Behavior[Update] = Behaviors.withTimers { timers =>
    timers.startTimerWithFixedDelay(Unfreeze, freezingTime.seconds)
    frozen()
  }

  /**
   * The [[BalloonActor]] is not gonna move while frozen, but can still be hit. It waits for an
   * [[Unfreeze]] message to keep on moving.
   */
  def frozen(): Behavior[Update] = Behaviors.withTimers { timers =>
    Behaviors.receiveMessagePartial {
      case Unfreeze =>
        timers.cancel(Unfreeze)
        default()

      case Hit(bullet, replyTo) =>
        hit(bullet, replyTo) { case _ =>
          Behaviors.same
        }

      case UpdateEntity(_, _, replyTo) =>
        replyTo ! EntityUpdated(balloon, ctx.self)
        Behaviors.same
    }
  }

  private def hit(bullet: Bullet, replyTo: ActorRef[Update])(
      bulletHandler: PartialFunction[Bullet, Behavior[Update]]): Behavior[Update] =
    balloon.pop(bullet) match {
      case None =>
        replyTo ! Gain(balloonHitGain)
        replyTo ! BalloonKilled(ctx.self)
        Behaviors.stopped
      case Some(b) =>
        if (b != balloon) replyTo ! Gain(balloonHitGain)
        balloon = b
        bulletHandler(bullet)
    }
}
