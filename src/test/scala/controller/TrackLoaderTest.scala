package controller

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import commons.Futures.retrieve
import controller.inout.FileCoders.CoderBuilder.appDir
import controller.inout.TrackLoader.TrackLoaderActor
import controller.inout.TrackLoader.TrackLoaderMessages._
import controller.interaction.Messages.Input
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps
import scala.reflect.io.Path

class TrackLoaderTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  Path(appDir).deleteRecursively() // Clean directories tree to setup testing environment

  val trackLoader: ActorRef[Input] = testKit.spawn(TrackLoaderActor())
  val controllerTest: TestProbe[Input] = testKit.createTestProbe[Input]()

  "The Track Loader" should {
    "clear saved tracks" in {
      trackLoader ! CleanSavedTracks(controllerTest.ref)
      controllerTest expectMessage SavedTracksCleared()
    }
    "be able to retrieve a previously saved track" in {
      retrieve(trackLoader ? (ref => RetrieveTrack(0, ref))) {
        case SavedTrack(track) =>
          track should not be null
        case _ => fail("A track was present but has not been retrieved")
      }
    }
    "be able to retrieve all the saved tracks" in {
      retrieve(trackLoader ? RetrieveSavedTracks) {
        case SavedTracks(tracks) =>
          tracks.isEmpty shouldBe false
        case _ => fail("A track was present but has not been retrieved")
      }
      trackLoader ! CleanSavedTracks(controllerTest.ref)
    }
  }
}
