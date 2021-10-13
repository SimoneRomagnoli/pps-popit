package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopMessages.{ MapCreated, Stop }
import controller.Messages._
import model.Model.ModelMessages._
import model.managers.{ EntitiesManager, EntityActor, SpawnManager }
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

/**
 * Model of the application, fundamental in the MVC pattern. It receives [[Update]] messages from
 * the game loop and updates the actors governing game entities.
 */
object Model {

  object ModelMessages {
    case class NewMap(replyTo: ActorRef[Input]) extends Update
    case class WalletQuantity(replyTo: ActorRef[Input]) extends Update
    case class Pay(amount: Int) extends Update
    case class Lose(amount: Int) extends Update
  }

  object ModelActor {

    def apply(controller: ActorRef[Input]): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx, controller).init()
    }
  }

  /**
   * The model actor has two behaviors:
   *   - init, in which it just starts a new game by creating a map and a set of managers;
   *   - default, in which it forwards the incoming messages to the correct manager.
   */
  case class ModelActor private (
      ctx: ActorContext[Update],
      controller: ActorRef[Input],
      stats: GameStats = GameStats(),
      var entities: List[EntityActor] = List(),
      var track: Track = Track(),
      var handlers: List[(ActorRef[Update], MessageType)] = List()) {

    def init(): Behavior[Update] = Behaviors.receiveMessage { case NewMap(replyTo) =>
      track = Track(gameGrid)
      handlers = (ctx.spawnAnonymous(SpawnManager(ctx.self, track)), SpawnMessage) :: handlers
      handlers = (ctx.spawnAnonymous(EntitiesManager(ctx.self, track)), EntityMessage) :: handlers

      replyTo ! MapCreated(track)
      default()
    }

    def default(): Behavior[Update] =
      Behaviors.receiveMessage {
        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        case Stop() =>
          Behaviors.stopped

        case msg =>
          handle(msg)
          Behaviors.same
      }

    def handle(msg: Update): Unit = msg match {
      case WithReplyTo(m, _) => choose(messageType(m)).foreach(_ ! msg)
      case msg               => choose(messageType(msg)).foreach(_ ! msg)
    }

    def choose(messageType: MessageType)(implicit
        handlers: List[(ActorRef[Update], MessageType)] = handlers): List[ActorRef[Update]] =
      handlers.collect {
        case (actorRef, msgType) if msgType == messageType => actorRef
      }
  }

}
