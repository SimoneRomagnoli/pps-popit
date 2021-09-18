package model

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages._
import model.Positions.Vector2D
import model.TowersTest._
import model.actors.TowerActor
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.towers.Towers.Tower
import org.scalatest.wordspec.AnyWordSpecLike

object TowersTest {

  val balloonPosition: Vector2D = (6.0, 8.0)
  val towerPosition: Vector2D = (2.0, 1.0)
  var collided: Boolean = false

  val tower: Tower = Tower(towerPosition)
  var balloon: Balloon = Simple(balloonPosition)

  val dummyBalloonActor: Balloon => Behavior[Update] = b =>
    Behaviors.receiveMessage {
      case UpdatePosition(replyTo) =>
        val newBalloon: Balloon = b in ((b.position.x - 1, b.position.y - 2))
        replyTo ! BalloonMoved(newBalloon)
        dummyBalloonActor(newBalloon)
      case _ => Behaviors.same
    }

  val dummyModel: ActorRef[Update] => Behavior[Update] = t =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Tick(replyTo) =>
          collided = false
          replyTo ! UpdatePosition(ctx.self)
          Behaviors.same
        case BalloonMoved(entity) =>
          t ! SearchBalloon(ctx.self, entity)
          Behaviors.same
        case CollisionDetected() =>
          collided = true
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
    "tower and balloon has just spawned" should {
      "be across the map and not collide" in {
        tower.position shouldBe towerPosition
        balloon.position shouldBe balloonPosition
        tower canSee balloon shouldBe false
      }
    }
    "balloon goes near the tower" should {
      "be in tower sight range and collide" in {
        balloon = balloon in ((3.0, 2.0))
        tower.position shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower canSee balloon shouldBe true
      }
    }
  }

  "During actor running" when {
    "actors has just spawned" should {
      "not collide" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        collided shouldBe false
      }
    }
    "balloon moves two times in direction of tower" should {
      "not collide at the beginning but after the second move" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        collided shouldBe true
      }
    }
    "balloon goes far from the tower" should {
      "not collide any more" in {
        model ! Tick(balloonActor)
        waitSomeTime()
        model ! Tick(balloonActor)
        waitSomeTime()
        collided shouldBe false
      }
    }
  }

}
