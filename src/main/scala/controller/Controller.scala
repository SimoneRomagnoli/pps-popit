package controller

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior, Scheduler }
import akka.util.Timeout
import controller.Controller.ControllerMessages._
import .TrackLoaderActor
import .TrackLoaderMessages._
import controller.interaction.GameLoop.GameLoopActor
import controller.interaction.GameLoop.GameLoopMessages.{ MapCreated, Start, Stop }
import controller.interaction.Messages._
import controller.settings.Settings.Time.TimeSettings
import controller.settings.Settings.{ Difficulty, Settings }
import model.Model.ModelActor
import model.entities.Entities.Entity
import model.managers.EntitiesMessages.PlaceTower
import model.managers.GameDynamicsMessages.{ CurrentGameTrack, CurrentTrack, NewMap }
import model.maps.Tracks.Track
import utils.Futures.retrieve
import view.View.ViewMessages.{ RenderMap, RenderSavedTracks, TrackSaved }

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Controller of the application, fundamental in the MVC pattern. It deals with inputs coming from
 * the view controllers.
 */
object Controller {

  object ControllerMessages {
    trait Navigation extends Input with Render
    trait SettingsMessage extends Input

    case class NewGame(withTrack: Option[Track]) extends Input with Render
    case class RetrieveAndLoadTrack(trackID: Int) extends Input
    case class BackToMenu() extends Navigation
    case class FinishGame() extends Input with Render
    case class SavedTracksPage() extends Navigation
    case class SettingsPage() extends Navigation
    case class PauseGame() extends Input with SpawnManagerMessage
    case class ResumeGame() extends Input with SpawnManagerMessage
    case class RestartGame() extends Input
    case class NewTrack() extends Input
    case class SetDifficulty(difficulty: Difficulty) extends SettingsMessage
    case class SetTimeRatio(timeRatio: Double) extends SettingsMessage
    case class SetFrameRate(frameRate: Double) extends SettingsMessage

    case class StartNextRound()
        extends Input
        with SpawnManagerMessage
        with GameDynamicsManagerMessage
        with EntitiesManagerMessage
    case class NewTimeRatio(value: Double) extends Input
    case class CurrentWallet(amount: Int) extends Input
    case class StartAnimation(entity: Entity) extends Render
    case class SaveCurrentTrack(pos: (Double, Double)) extends Input

    sealed trait Interaction extends Input {
      def replyTo: ActorRef[Message]
      def request: Message
    }

    case class ActorInteraction(
        override val replyTo: ActorRef[Message],
        override val request: Message)
        extends Interaction
  }

  object ControllerActor {

    def apply(view: ActorRef[Render]): Behavior[Input] = Behaviors.setup { ctx =>
      ControllerActor(ctx, view, ctx.spawnAnonymous(TrackLoaderActor())).default()
    }
  }

  /**
   * The controller actor has two behaviors:
   *   - default, in which it simply receives input messages and satisfies them;
   *   - interacting, in which it has to respond to another subscribed actor that needs a response
   *     (mostly the view requiring information from the model).
   */
  case class ControllerActor private (
      ctx: ActorContext[Input],
      view: ActorRef[Render],
      trackLoader: ActorRef[Input],
      var settings: Settings = Settings(),
      var model: Option[ActorRef[Update]] = None,
      var gameLoop: Option[ActorRef[Input]] = None) {
    implicit val timeout: Timeout = Timeout(1.seconds)
    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case NewGame(withTrack) =>
        view ! NewGame(withTrack)
        model = Some(ctx.spawnAnonymous(ModelActor(settings)))
        gameLoop = Some(ctx.spawnAnonymous(GameLoopActor(model.get, view, settings.timeSettings)))
        model.get ! NewMap(ctx.self, withTrack)
        gameLoop.get ! Start()
        Behaviors.same

      case NewTrack() =>
        model.get ! NewMap(ctx.self, None)
        Behaviors.same

      case RetrieveAndLoadTrack(trackID) =>
        retrieve(trackLoader ? (self => RetrieveTrack(trackID, self))) {
          case SavedTrack(track) =>
            ctx.self ! NewGame(Some(track))
          case _ =>
        }
        Behaviors.same

      case SaveCurrentTrack((posX, posY)) =>
        retrieve(model.get ? CurrentGameTrack) { case CurrentTrack(track) =>
          trackLoader ! SaveActualTrack(track, posX, posY, ctx.self)
        }
        Behaviors.same

      case TrackSaved() =>
        view ! TrackSaved()
        Behaviors.same

      case MapCreated(track) =>
        view ! RenderMap(track)
        Behaviors.same

      case RestartGame() =>
        retrieve(model.get ? CurrentGameTrack) {
          case CurrentTrack(track) =>
            clearModelAndGameLoop()
            ctx.self ! NewGame(Some(track))
          case _ =>
        }
        Behaviors.same

      case StartNextRound() =>
        model.get ! StartNextRound()
        Behaviors.same

      case ActorInteraction(replyTo, message) =>
        model.get ! WithReplyTo(message.asInstanceOf[Update], ctx.self)
        interacting(replyTo)

      case PlaceTower(cell, towerType) =>
        model.get ! WithReplyTo(PlaceTower(cell, towerType), ctx.self)
        Behaviors.same

      case SavedTracksPage() =>
        retrieve(trackLoader ? RetrieveSavedTracks) { case SavedTracks(tracks) =>
          view ! RenderSavedTracks(tracks)
        }
        Behaviors.same

      case navigation: Navigation =>
        view ! navigation
        clearModelAndGameLoop()
        Behaviors.same

      case settingsMessage: SettingsMessage =>
        settingsMessage match {
          case SetDifficulty(difficulty) =>
            settings = Settings(difficulty, settings.timeSettings)
          case SetTimeRatio(timeRatio) =>
            settings = Settings(
              settings.difficulty,
              TimeSettings(settings.timeSettings.frameRate, timeRatio)
            )
          case SetFrameRate(frameRate) =>
            settings = Settings(
              settings.difficulty,
              TimeSettings(frameRate, settings.timeSettings.timeRatio)
            )
        }
        Behaviors.same

      case input: Input if input.isInstanceOf[PauseGame] || input.isInstanceOf[ResumeGame] =>
        gameLoop.get ! input
        Behaviors.same

      case _ => Behaviors.same

    }

    def interacting(replyTo: ActorRef[Message]): Behavior[Input] = Behaviors.receiveMessage {
      message =>
        replyTo ! message
        default()
    }

    private def clearModelAndGameLoop(): Unit = if (gameLoop.isDefined || model.isDefined) {
      model.get ! Stop()
      gameLoop.get ! Stop()
      gameLoop = None
      model = None
    }
  }
}
