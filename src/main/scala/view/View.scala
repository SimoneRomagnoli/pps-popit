package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Controller.ControllerMessages.{
  ExitGame,
  HighScoresPage,
  NewGame,
  StartAnimation
}
import controller.GameLoop.GameLoopMessages.CanStartNextRound
import controller.Messages.{ Input, Render }
import model.entities.Entities.Entity
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid
import view.View.ViewMessages._
import view.controllers.{
  ViewGameController,
  ViewMainController,
  ViewMainMenuController,
  ViewSavedTracksController
}

import scala.language.{ existentials, reflectiveCalls }

/**
 * View of the application, fundamental in the MVC pattern. It receives [[Render]] messages from the
 * game loop and renders their content via fxml controllers.
 */
object View {

  object ViewMessages {
    case class RenderStats(stats: GameStats) extends Render
    case class RenderGameOver() extends Render
    case class RenderEntities(entities: List[Entity]) extends Render
    case class RenderMap(track: Track) extends Render
    case class TrackSaved() extends Render with Input
  }

  object ViewActor {

    def apply(mainController: ViewMainController): Behavior[Render] = Behaviors.setup { _ =>
      new ViewActor(mainController).inMenu(mainController.menuController)
    }
  }

  /**
   * Thew view actor has two behaviors:
   *   - inGame, in which updates a [[ViewGameController]];
   *   - inMenu, in which updates another controller.
   */
  class ViewActor private (mainController: ViewMainController) {

    def inMenu(menuController: ViewMainMenuController): Behavior[Render] = {
      menuController.show()
      Behaviors.receiveMessage {
        case NewGame(_) =>
          menuController.hide()
          inGame(mainController.gameController)

        case HighScoresPage() =>
          menuController.hide()
          inPodium(mainController.savedTracksController)

        case _ => Behaviors.same
      }
    }

    def inGame(gameController: ViewGameController): Behavior[Render] = {
      gameController.setup()
      gameController.show()
      Behaviors.receiveMessage {
        case RenderStats(stats) =>
          gameController render stats
          Behaviors.same

        case RenderEntities(entities: List[Entity]) =>
          gameController draw entities
          Behaviors.same

        case RenderMap(track) =>
          gameController.reset()
          gameController draw gameGrid
          gameController draw track
          Behaviors.same

        case TrackSaved() =>
          gameController.showGameEntities()
          gameController.pauseController.show()
          Behaviors.same
        case StartAnimation(entity) =>
          gameController animate entity
          Behaviors.same

        case CanStartNextRound() =>
          gameController.gameMenuController.enableRoundButton()
          Behaviors.same

        case RenderGameOver() =>
          gameController.gameOverController.show()
          Behaviors.same

        case ExitGame() =>
          gameController.hide()
          inMenu(mainController.menuController)

        case _ => Behaviors.same
      }
    }

    def inPodium(savedTrackController: ViewSavedTracksController): Behavior[Render] = {
      savedTrackController.show()
      Behaviors.receiveMessage { case _ =>
        Behaviors.same
      }
    }
  }

}
