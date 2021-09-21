package model

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import controller.Messages
import controller.Messages.{ EntityUpdated, TickUpdate, Update, UpdateEntity }
import model.entities.Entities.Entity

object Model {

  object ModelActor {

    def apply(): Behavior[Update] = Behaviors.setup { ctx =>
      running(ctx, List(), Seq())
    }

    def running(
        ctx: ActorContext[Update],
        entities: List[Entity],
        actors: Seq[ActorRef[Update]]): Behavior[Update] =
      Behaviors.receiveMessage { case TickUpdate(elapsedTime, replyTo) =>
        actors foreach {
          _ ! UpdateEntity(elapsedTime, entities, ctx.self)
        }
        updating(ctx, entities, actors, replyTo)
      }

    def updating(
        ctx: ActorContext[Update],
        entities: List[Entity],
        actors: Seq[ActorRef[Update]],
        replyTo: ActorRef[Messages.Input]): Behavior[Update] =
      Behaviors.receiveMessage {
        case EntityUpdated(_) =>
          running(ctx, entities, actors)
        case _ => Behaviors.same
      }
  }

}
