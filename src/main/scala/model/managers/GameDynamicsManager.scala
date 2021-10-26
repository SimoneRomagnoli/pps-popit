package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.{ CurrentWallet, StartNextRound }
import controller.interaction.GameLoop.GameLoopMessages.{ GameOver, GameStatsUpdated, MapCreated }
import controller.interaction.Messages.{ GameDynamicsManagerMessage, Input, Update }
import controller.settings.Settings.Settings
import model.Model.ModelMessages.{ TickUpdate, TrackChanged }
import model.managers.GameDynamicsMessages.{
  CurrentGameTrack,
  CurrentTrack,
  Gain,
  Lose,
  NewMap,
  Pay,
  WalletQuantity
}
import model.maps.Plots.{ Plotter, PrologPlotter }
import model.maps.Tracks.Track
import model.stats.Stats.GameStats

object GameDynamicsMessages {

  case class NewMap(replyTo: ActorRef[Input], withTrack: Option[Track])
      extends Update
      with GameDynamicsManagerMessage
  case class WalletQuantity(replyTo: ActorRef[Input]) extends Update with GameDynamicsManagerMessage

  case class CurrentGameTrack(replyTo: ActorRef[Input])
      extends Update
      with GameDynamicsManagerMessage
  case class CurrentTrack(track: Track) extends Input
  case class Pay(amount: Int) extends Update with GameDynamicsManagerMessage
  case class Gain(amount: Int) extends Update with GameDynamicsManagerMessage
  case class Lose(amount: Int) extends Update with GameDynamicsManagerMessage
}

object GameDynamicsManager {

  def apply(model: ActorRef[Update], settings: Settings): Behavior[Update] =
    Behaviors.setup { ctx =>
      DynamicsManager(ctx, model, settings).default()
    }
}

case class DynamicsManager private (
    ctx: ActorContext[Update],
    model: ActorRef[Update],
    settings: Settings,
    stats: GameStats = GameStats(),
    plotter: Plotter = PrologPlotter(),
    var track: Track = Track()) {

  def default(): Behavior[Update] = Behaviors.receiveMessage {
    case StartNextRound() =>
      stats.nextRound()
      Behaviors.same

    case NewMap(replyTo, withTrack) =>
      withTrack match {
        case Some(t) => track = t
        case _       => track = Track(plotter.plot)
      }
      replyTo ! MapCreated(track)
      model ! TrackChanged(track)
      Behaviors.same

    case WalletQuantity(replyTo) =>
      replyTo ! CurrentWallet(stats.wallet)
      Behaviors.same

    case CurrentGameTrack(replyTo) =>
      replyTo ! CurrentTrack(track)
      Behaviors.same

    case Pay(amount) =>
      stats pay amount
      Behaviors.same

    case Gain(amount) =>
      stats gain amount
      Behaviors.same

    case Lose(amount) =>
      stats lose amount
      Behaviors.same

    case TickUpdate(_, replyTo) =>
      stats.life match {
        case x if x <= 0 => replyTo ! GameOver()
        case _           => replyTo ! GameStatsUpdated(stats)
      }
      Behaviors.same

    case _ => Behaviors.same
  }
}
