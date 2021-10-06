package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ EntitySpawned, Update }
import model.actors.SpawnerActorTest.{ balloonsSpawned, dummyModel, waitSomeTime }
import model.entities.balloons.BalloonDecorations.Regenerating
import model.entities.balloons.BalloonLives._
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.BalloonsFactory.RichBalloon
import model.spawn.SpawnManager.{ Round, Streak }
import model.spawn.SpawnerMonad.{ add, RichIO }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object SpawnerActorTest {
  var balloonsSpawned: List[Balloon] = List()

  val dummyModel: Behavior[Update] = Behaviors.receiveMessage {
    case EntitySpawned(b, _) =>
      balloonsSpawned = b.asInstanceOf[Balloon] :: balloonsSpawned
      Behaviors.same
    case _ => Behaviors.same
  }

  def waitSomeTime(): Unit = Thread.sleep(3000)
}

class SpawnerActorTest
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterEach {
  val model: ActorRef[Update] = testKit.spawn(dummyModel)
  val spawner: ActorRef[Update] = testKit.spawn(SpawnerActor(model))
  val nBalloons: Int = 5
  val simpleRound: Round = Round(Seq(Streak(nBalloons)))

  val simpleRoundBalloons: List[Balloon] =
    LazyList.iterate(Red balloon)(b => b).take(nBalloons).toList

  val complexRound: Round = (for {
    _ <- add(Streak(nBalloons) :- Red)
    _ <- add(Streak(nBalloons) :- (Blue & Regenerating & Regenerating & Regenerating))
    _ <- add((Streak(nBalloons) :- Green) @@ 50.milliseconds)
  } yield ()).get

  val complexRoundBalloons: List[Balloon] =
    (LazyList.iterate(Red balloon)(b => b).take(nBalloons).toList appendedAll
      LazyList
        .iterate((Blue balloon) adding Regenerating)(b => b)
        .take(nBalloons)
        .toList appendedAll
      LazyList.iterate(Green balloon)(b => b).take(nBalloons).toList).reverse

  override def beforeEach(): Unit = balloonsSpawned = List()

  "The spawner" when {
    "the round starts" should {
      "spawn all the balloons of a simple round" in {
        spawner ! StartRound(simpleRound)
        waitSomeTime()
        balloonsSpawned shouldBe simpleRoundBalloons
      }
      "spawn all the balloons of a more complex round" in {
        spawner ! StartRound(complexRound)
        waitSomeTime()
        balloonsSpawned.size shouldBe complexRoundBalloons.size
      }
    }
  }
}
