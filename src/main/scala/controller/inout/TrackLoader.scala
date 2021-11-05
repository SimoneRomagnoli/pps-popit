package controller.inout

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import commons.CommonValues.View.{ gameBoardHeight, gameBoardWidth }
import controller.Controller
import controller.inout.FileCoders.CoderBuilder.imagesDir
import controller.inout.TrackLoader.TrackLoaderMessages._
import controller.interaction.Messages.Input
import model.maps.Tracks.Track
import view.View.ViewMessages.TrackSaved

import java.awt.{ GraphicsEnvironment, Rectangle, Robot }
import java.io.File
import javax.imageio.ImageIO
import controller.inout.FileCoders.CoderBuilder.separator

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
    case class CleanSavedTracks() extends Input
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

    def default(): Behavior[Input] = Behaviors.receiveMessagePartial {

      case CleanSavedTracks() =>
        coder.clean()
        Behaviors.same

      case SaveActualTrack(track, x, y, replyTo) =>
        var savedTracks: List[Track] = coder.deserialize()
        savedTracks = savedTracks appended track
        ScreenShooter.takeScreen(x, y, savedTracks.size - 1)
        coder.serialize(savedTracks)
        replyTo ! TrackSaved()
        Behaviors.same

      case RetrieveSavedTracks(replyTo) =>
        replyTo ! SavedTracks(coder.deserialize())
        Behaviors.same

      case RetrieveTrack(trackID, replyTo) =>
        replyTo ! SavedTrack(coder.deserialize()(trackID))
        Behaviors.same
    }
  }

  /**
   * Utils object that takes a screenshot of the boardGame to create the SavedTracksPage.
   */
  object ScreenShooter {

    def takeScreen(x: Double, y: Double, index: Int): Unit = if (!GraphicsEnvironment.isHeadless) {
      val rectangle =
        new Rectangle(x.toInt, y.toInt, gameBoardWidth.toInt, gameBoardHeight.toInt)
      val bufferedImage = (new Robot).createScreenCapture(rectangle)
      ImageIO.write(
        bufferedImage,
        "png",
        new File(imagesDir + separator + "track" + index + ".png")
      )
    }
  }
}
