package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.interaction.GameLoop.GameLoopMessages.Stop
import controller.interaction.Messages._
import controller.settings.Settings.Settings
import model.managers.{ EntitiesManager, GameDataManager, SpawnManager }
import model.maps.Tracks.Track

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
        with GameDataManagerMessage

    case class TrackChanged(newTrack: Track)
        extends Update
        with EntitiesManagerMessage
        with SpawnManagerMessage
  }

  object ModelActor {

    def apply(settings: Settings): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx).init(settings)
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

    def init(settings: Settings): Behavior[Update] = {
      handlers =
        (ctx.spawnAnonymous(SpawnManager(ctx.self, settings.timeSettings)), SpawnMessage) ::
          (ctx.spawnAnonymous(EntitiesManager(ctx.self)), EntityMessage) ::
          (ctx.spawnAnonymous(GameDataManager(ctx.self, settings)), GameDataMessage) :: handlers
      default()
    }

    def default(): Behavior[Update] =
      Behaviors.receiveMessage {
        case Stop() =>
          Behaviors.stopped

        case msg =>
          forward(msg)
          Behaviors.same
      }

    def forward(msg: Update): Unit = msg match {
      case ActualMessage(m) => choose(messageTypes(m)).foreach(_ ! msg)
      case _                =>
    }

    def choose(messageTypes: List[MessageType])(implicit
        handlers: List[(ActorRef[Update], MessageType)] = handlers): List[ActorRef[Update]] =
      handlers.collect {
        case (actorRef, msgType) if messageTypes contains msgType => actorRef
      }
  }

}
