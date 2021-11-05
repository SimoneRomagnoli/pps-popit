package model.actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.{ PauseGame, ResumeGame, StartNextRound }
import controller.interaction.Messages.Update
import controller.settings.Settings.Time.TimeSettings
import model.Model.ModelMessages.TrackChanged
import model.actors.SpawnManagerTest.{ balloonsSpawned, dummyModel, waitSomeTime }
import model.entities.balloons.BalloonDecorations.{ Camo, Lead, Regenerating }
import model.entities.balloons.BalloonLives._
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.managers.EntitiesMessages.EntitySpawned
import model.managers.SpawnManager
import model.managers.SpawnerMessages.StartRound
import model.maps.Cells.Cell
import model.maps.Tracks.Track
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }
import model.spawn.RoundsFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object SpawnManagerTest {
  var balloonsSpawned: List[Balloon] = List()

  val dummyModel: Behavior[Update] = Behaviors.receiveMessage {
    case EntitySpawned(b, _) =>
      balloonsSpawned = b.asInstanceOf[Balloon] :: balloonsSpawned
      Behaviors.same
    case _ => Behaviors.same
  }

  def waitSomeTime(): Unit = Thread.sleep(3000)
}

class SpawnManagerTest
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterEach {
  val model: ActorRef[Update] = testKit.spawn(dummyModel)

  val nBalloons: Int = 5
  val simpleRound: Round = Round.of(Streak(nBalloons))

  val simpleRoundBalloons: List[Balloon] =
    LazyList.iterate(Red balloon)(b => b).take(nBalloons).toList

  val complexRound: Round = (for {
    _ <- add((Streak(nBalloons) :- Red) @@ 50.milliseconds)
    _ <- add((Streak(nBalloons) :- (Blue & Camo & Regenerating)) @@ 50.milliseconds)
    _ <- add((Streak(nBalloons) :- (Green & Lead & Camo)) @@ 50.milliseconds)
  } yield ()).get

  val complexRoundBalloons: List[Balloon] =
    (LazyList.iterate(Red balloon)(b => b).take(nBalloons).toList appendedAll
      LazyList
        .iterate((Blue balloon) adding List(Regenerating, Camo))(b => b)
        .take(nBalloons)
        .toList appendedAll
      LazyList
        .iterate((Green balloon) adding List(Camo, Lead))(b => b)
        .take(nBalloons)
        .toList).reverse

  override def beforeEach(): Unit = balloonsSpawned = List()

  "The spawner" when {
    "told the start the next round" should {
      val spawner: ActorRef[Update] = testKit.spawn(SpawnManager(model, TimeSettings()))
      "start the round" in {
        RoundsFactory.startGame()
        RoundsFactory.currentRound shouldBe 0
        spawner ! StartNextRound()
        waitSomeTime()
        RoundsFactory.currentRound shouldBe 1
      }
    }
    "the track changes" should {
      val spawner: ActorRef[Update] = testKit.spawn(SpawnManager(model, TimeSettings()))
      val track: Track = Track(Seq(Cell(0, 0)))
      "update the track" in {
        spawner ! TrackChanged(track)
        spawner ! StartRound(simpleRound)
        waitSomeTime()
        balloonsSpawned.foreach(_.track shouldBe track)
      }
    }
    "the round starts" should {
      val spawner: ActorRef[Update] = testKit.spawn(SpawnManager(model, TimeSettings()))
      "spawn all the balloons of a simple round" in {
        spawner ! StartRound(simpleRound)
        waitSomeTime()
        balloonsSpawned.map(_ in (0.0, 0.0)) shouldBe simpleRoundBalloons
      }
      "spawn all the balloons of a more complex round" in {
        spawner ! StartRound(complexRound)
        waitSomeTime()
        balloonsSpawned.map(_ in (0.0, 0.0)) shouldBe complexRoundBalloons
      }
    }
    "spawning" should {
      val spawner: ActorRef[Update] = testKit.spawn(SpawnManager(model, TimeSettings()))
      "be able to be paused and resumed" in {
        spawner ! StartRound(complexRound)
        spawner ! PauseGame()
        waitSomeTime()
        balloonsSpawned.size should be < complexRoundBalloons.size
        spawner ! ResumeGame()
        waitSomeTime()
        balloonsSpawned.map(_ in (0.0, 0.0)) shouldBe complexRoundBalloons
      }
    }
  }
}
