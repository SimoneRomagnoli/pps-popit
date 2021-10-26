package controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages.Input
import controller.TrackLoader.TrackLoaderMessages.{
  RetrieveSavedTracks,
  SaveActualTrack,
  SavedTracks
}
import controller.files.FileCoder
import model.maps.Tracks.Track
import utils.ScreenShooter
import view.View.ViewMessages.TrackSaved

object TrackLoader {

  object TrackLoaderMessages {

    case class SaveActualTrack(track: Track, posX: Double, posY: Double, replyTo: ActorRef[Input])
        extends Input

    case class RetrieveSavedTracks(replyTo: ActorRef[Input]) extends Input
    //case class RetrieveTrack(replyTo: ActorRef[Input]) extends Input
    case class SavedTracks(list: List[Track]) extends Input
    //case class SavedTrack(track: Track) extends Input
  }

  object TrackLoaderActor {

    def apply(): Behavior[Input] = Behaviors.setup { ctx =>
      new TrackLoaderActor().default()
    }
  }

  case class TrackLoaderActor(coder: FileCoder = FileCoder()) {

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

//      case RetrieveTrack(replyTo) =>
//        replyTo ! SavedTrack(savedTracks(0))
//        Behaviors.same

      case _ =>
        Behaviors.same
    }
  }

}
