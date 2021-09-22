package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages._
import model.Positions.Vector2D
import model.actors.TowerActor
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.towers.Towers.Tower
import model.entities.towers.TowersTest.{
  balloon,
  balloonDetected,
  balloonPosition,
  dummyBalloonActor,
  dummyModel,
  tower,
  towerPosition,
  waitSomeTime
}
import org.scalatest.wordspec.AnyWordSpecLike

object TowersTest {

  val balloonPosition: Vector2D = (60.0, 80.0)
  val towerPosition: Vector2D = (20.0, 10.0)
  var balloonDetected: Boolean = false

  val tower: Tower = Tower(towerPosition)
  var balloon: Balloon = Simple(balloonPosition)

  val dummyBalloonActor: Balloon => Behavior[Update] = b =>
    Behaviors.receiveMessage {
      case UpdatePosition(replyTo) =>
        val newBalloon: Balloon = b in ((b.position.x - 20, b.position.y - 30))
        replyTo ! BalloonMoved(newBalloon)
        dummyBalloonActor(newBalloon)
      case _ => Behaviors.same
    }

  val dummyModel: ActorRef[Update] => Behavior[Update] = t =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Tick(replyTo) =>
          balloonDetected = false
          replyTo ! UpdatePosition(ctx.self)
          Behaviors.same
        case BalloonMoved(entity) =>
          t ! SearchBalloon(ctx.self, entity)
          Behaviors.same
        case BalloonDetected() =>
          balloonDetected = true
          Behaviors.same
        case _ => Behaviors.same
      }
    }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class TowersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val towerActor: ActorRef[Update] = testKit.spawn(TowerActor(Tower(towerPosition)))
  val model: ActorRef[Update] = testKit.spawn(dummyModel(towerActor))

  val balloonActor: ActorRef[Update] =
    testKit.spawn(dummyBalloonActor(Simple(balloonPosition)))

  "During local running" when {
    "tower and balloon has just spawned, the tower" should {
      "not see the balloon because it is across the map" in {
        tower.position shouldBe towerPosition
        balloon.position shouldBe balloonPosition
        tower canSee balloon shouldBe false
      }
    }
    "the balloon goes near the tower, it" should {
      "see the balloon because it is in tower's sight range" in {
        balloon = balloon in ((3.0, 2.0))
        tower.position shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower canSee balloon shouldBe true
      }
    }
  }

  "During actor running" when {
    "actors has just spawned, the tower" should {
      "not see the balloon" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
    "the balloon moves through the map, the tower" should {
      "not see the balloon immediately, but after the second move" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe true
      }
    }
    "the balloon goes far away, the tower" should {
      "not see the balloon any more" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
  }

}
