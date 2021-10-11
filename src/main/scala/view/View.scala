package view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.Render
import model.entities.Entities.Entity
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid
import view.View.ViewMessages._
import view.controllers.{ ViewGameController, ViewMainController }

import scala.language.reflectiveCalls

/**
 * View of the application, fundamental in the MVC pattern. It receives [[Render]] messages from the
 * game loop and renders their content via fxml controllers.
 */
object View {

  object ViewMessages {
    case class RenderStats(stats: GameStats) extends Render
    case class RenderEntities(entities: List[Entity]) extends Render
    case class RenderMap(track: Track) extends Render
  }

  object ViewActor {

    def apply(mainController: ViewMainController): Behavior[Render] = Behaviors.setup { _ =>
      new ViewActor(mainController).inGame(mainController.gameController)
    }
  }

  /**
   * Thew view actor has two behaviors:
   *   - inGame, in which updates a [[ViewGameController]];
   *   - inMenu, in which updates another controller.
   */
  class ViewActor private (mainController: ViewMainController) {

    def inGame(gameController: ViewGameController): Behavior[Render] =
      Behaviors.receiveMessage {
        case RenderStats(stats) =>
          gameController update stats
          Behaviors.same

        case RenderEntities(entities: List[Entity]) =>
          gameController draw entities
          Behaviors.same

        case RenderMap(track) =>
          gameController.reset()
          gameController draw gameGrid
          gameController draw track
          Behaviors.same

        case _ => Behaviors.same
      }
  }

}
