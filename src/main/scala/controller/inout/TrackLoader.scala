package controller.inout

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller
import controller.inout.TrackLoader.TrackLoaderMessages.{
  RetrieveSavedTracks,
  RetrieveTrack,
  SaveActualTrack,
  SavedTrack,
  SavedTracks
}
import controller.interaction.Messages.Input
import model.maps.Tracks.Track
import utils.ScreenShooter
import view.View.ViewMessages.TrackSaved

/**
 * The Track Loader is the component that interacts with the [[Controller]] and the [[FileCoder]].
 * It is responsible for retrieving from a file the previously saved tracks and saving newer ones.
 */
object TrackLoader {

  object TrackLoaderMessages {

    case class SaveActualTrack(track: Track, posX: Double, posY: Double, replyTo: ActorRef[Input])
        extends Input

    case class RetrieveSavedTracks(replyTo: ActorRef[Input]) extends Input
    case class RetrieveTrack(trackID: Int, replyTo: ActorRef[Input]) extends Input
    case class SavedTracks(list: List[Track]) extends Input
    case class SavedTrack(track: Track) extends Input
  }

  object TrackLoaderActor {

    def apply(): Behavior[Input] = Behaviors.setup { ctx =>
      new TrackLoaderActor(ctx).default()
    }
  }

  /**
   * It represents the track loader behavior
   * @param ctx
   *   the actors system context
   * @param coder
   *   the [[FileCoder]] used to handle save and load operation on the json file
   */
  case class TrackLoaderActor(ctx: ActorContext[Input], coder: FileCoder = FileCoder()) {

    var savedTracks: List[Track] = List()
    var actualTrack: Track = Track()

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case SaveActualTrack(track, x, y, replyTo) =>
        if (savedTracks.isEmpty) savedTracks = coder.deserialize()
        savedTracks = savedTracks.appended(track)
        ScreenShooter.takeScreen(x, y, savedTracks.size - 1)
        coder.serialize(savedTracks)
        replyTo ! TrackSaved()
        Behaviors.same

      case RetrieveSavedTracks(replyTo) =>
        if (savedTracks.isEmpty) savedTracks = coder.deserialize()
        replyTo ! SavedTracks(savedTracks)
        Behaviors.same

      case RetrieveTrack(trackID, replyTo) =>
        replyTo ! SavedTrack(savedTracks(trackID))
        Behaviors.same

      case _ =>
        Behaviors.same
    }
  }

}
