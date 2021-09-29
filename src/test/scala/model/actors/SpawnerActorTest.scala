package model.actors

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import controller.Messages.{ EntitySpawned, Update }
import model.actors.SpawnerActorTest.{ dummyModel, spawnCounter, waitSomeTime }
import model.entities.balloons.BalloonType.{ Plain, Red }
import model.spawn.SpawnManager.{ Round, Streak }
import org.junit.jupiter.api.BeforeEach
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt

object SpawnerActorTest {
  var spawnCounter: Int = 0

  val dummyModel: Behavior[Update] = Behaviors.receiveMessage {
    case EntitySpawned(_, _) =>
      spawnCounter = spawnCounter + 1
      Behaviors.same
    case _ => Behaviors.same
  }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class SpawnerActorTest
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterEach {
  val model: ActorRef[Update] = testKit.spawn(dummyModel)
  val spawner: ActorRef[Update] = testKit.spawn(SpawnerActor(model))
  val simpleRound: Round = Round(Seq(Streak(Plain, Red, 5, 10.milliseconds)))

  val complexRound: Round = Round(
    Seq(
      Streak(Plain, Red, 5, 10.milliseconds),
      Streak(Plain, Red, 5, 10.milliseconds),
      Streak(Plain, Red, 5, 10.milliseconds)
    )
  )

  override def beforeEach(): Unit = spawnCounter = 0

  "The spawner" when {
    "The round starts" should {
      "Spawn all the balloons of a simple round" in {
        spawner ! StartRound(simpleRound)
        waitSomeTime()
        spawnCounter shouldBe simpleRound.streaks.map(_.quantity).sum
      }
      "Spawn all the balloons of a more complex round" in {
        spawner ! StartRound(complexRound)
        waitSomeTime()
        spawnCounter shouldBe complexRound.streaks.map(_.quantity).sum
      }
    }
  }
}
