package model

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import controller.GameLoop.GameLoopMessages.Stop
import controller.Messages._
import model.managers.{EntitiesManager, GameDynamicsManager, SpawnManager}
import model.maps.Tracks.Track
import model.spawn.RoundsFactory

import scala.language.postfixOps

/**
 * Model of the application, fundamental in the MVC pattern. It receives [[Update]] messages from
 * the game loop and updates the actors governing game entities.
 */
object Model {

  object ModelMessages {

    case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input])
        extends Update
        with EntitiesManagerMessage
        with GameDynamicsManagerMessage

    case class TrackChanged(newTrack: Track)
        extends Update
        with EntitiesManagerMessage
        with SpawnManagerMessage
  }

  object ModelActor {

    def apply(): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx).init()
    }
  }

  /**
   * The model actor has two behaviors:
   *   - init, in which it just starts a new game by creating a map and a set of managers;
   *   - default, in which it forwards the incoming messages to the correct manager.
   */
  case class ModelActor private (
      ctx: ActorContext[Update],
      var handlers: List[(ActorRef[Update], MessageType)] = List()) {

    def init(): Behavior[Update] = {
      handlers = (ctx.spawnAnonymous(SpawnManager(ctx.self)), SpawnMessage) :: handlers
      handlers = (ctx.spawnAnonymous(EntitiesManager(ctx.self)), EntityMessage) :: handlers
      handlers =
        (ctx.spawnAnonymous(GameDynamicsManager(ctx.self)), GameDynamicsMessage) :: handlers
      default()
    }

    def default(): Behavior[Update] =
      Behaviors.receiveMessage {
        case Stop() =>
          Behaviors.stopped

        case msg =>
          handle(msg)
          Behaviors.same
      }

    def handle(msg: Update): Unit = msg match {
      case WithReplyTo(m, _) => choose(messageTypes(m)).foreach(_ ! msg)
      case msg               => choose(messageTypes(msg)).foreach(_ ! msg)
    }

    def choose(messageTypes: List[MessageType])(implicit
        handlers: List[(ActorRef[Update], MessageType)] = handlers): List[ActorRef[Update]] =
      handlers.collect {
        case (actorRef, msgType) if messageTypes contains msgType => actorRef
      }
  }

}
