package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.CurrentWallet
import controller.GameLoop.GameLoopMessages.MapCreated
import controller.Messages.{ GameDynamicsManagerMessage, Input, Update }
import model.Model.ModelMessages.{ TrackChangedForEntitiesManager, TrackChangedForSpawnManager }
import model.managers.GameDynamicsMessages.{ NewMap, Pay, WalletQuantity }
import model.maps.Plots.{ Plotter, PrologPlotter }
import model.maps.Tracks.Track
import model.stats.Stats.GameStats

object GameDynamicsMessages {
  case class NewMap(replyTo: ActorRef[Input]) extends Update with GameDynamicsManagerMessage
  case class WalletQuantity(replyTo: ActorRef[Input]) extends Update with GameDynamicsManagerMessage
  case class Pay(amount: Int) extends Update with GameDynamicsManagerMessage
  case class Lose(amount: Int) extends Update with GameDynamicsManagerMessage
}

object GameDynamicsManager {

  def apply(model: ActorRef[Update]): Behavior[Update] = Behaviors.setup { ctx =>
    DynamicsManager(ctx, model).default()
  }
}

case class DynamicsManager private (
    ctx: ActorContext[Update],
    model: ActorRef[Update],
    stats: GameStats = GameStats(),
    plotter: Plotter = PrologPlotter()) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case NewMap(replyTo) =>
      val track: Track = Track(plotter.plot)
      replyTo ! MapCreated(track)
      model ! TrackChangedForEntitiesManager(track)
      model ! TrackChangedForSpawnManager(track)
      Behaviors.same

    case WalletQuantity(replyTo) =>
      replyTo ! CurrentWallet(stats.wallet)
      Behaviors.same

    case Pay(amount) =>
      stats spend amount
      Behaviors.same

    case _ => Behaviors.same
  }
}