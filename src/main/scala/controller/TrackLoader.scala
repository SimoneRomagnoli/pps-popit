package controller

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import controller.Controller.ControllerActor
import controller.Messages.{ Input, Render, Update }
import controller.TrackLoader.TrackLoaderMessages.SaveActualTrack
import model.maps.Tracks.Track
import utils.ScreenShooter
import view.View.ViewMessages.TrackSaved

import scala.reflect.io.File

object TrackLoader {

  object TrackLoaderMessages {

    case class SaveActualTrack(track: Track, posX: Double, posY: Double, replyTo: ActorRef[Input])
        extends Input
  }

  // caricare da file le tracce e renderle disponibili alla pagina
  // aggiungere eventualmente sul file una nuova mappa + screenshot
  object TrackLoaderActor {

    def apply(): Behavior[Input] = Behaviors.setup { ctx =>
      new TrackLoaderActor().default()
    }
  }

  case class TrackLoaderActor() {

    //val file: File = ???
    //var savedTracks: List[Track] = ??? //Deserializer
    var actualTrack: Track = Track()

    // String path image
    //val tracks: Map[Int, String] = ???

    def default(): Behavior[Input] = Behaviors.receiveMessage {
      case SaveActualTrack(track, x, y, replyTo) =>
        actualTrack = track
        println(actualTrack)
        ScreenShooter.takeScreen(x, y)
        replyTo ! TrackSaved()
        Behaviors.same
      case _ =>
        Behaviors.same
    }
  }

}
