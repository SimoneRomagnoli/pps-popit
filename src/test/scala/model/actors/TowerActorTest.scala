package model.actors

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages._
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.towers.Towers.Tower
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.values
import model.entities.bullets.Bullets.{ Bullet, Dart }
import model.Positions.Vector2D
import model.actors.TestMessages.{ BalloonMoved, Step, UpdatePosition }
import model.actors.TowerActorTest._
import model.managers.EntitiesMessages.{ EntityUpdated, UpdateEntity }
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.Entities.Towers.towerDefaultDirection

import scala.language.postfixOps

object TestMessages {
  case class UpdatePosition(replyTo: ActorRef[Update]) extends Update

  case class Step(replyTo: ActorRef[Update]) extends Update

  case class BalloonMoved(balloon: Balloon) extends Update
}

object TowerActorTest {

  val balloonPosition: Vector2D = (6.0, 6.0)
  val balloonBoundary: (Double, Double) = (1.0, 1.0)
  val towerPosition: Vector2D = (0.0, 0.0)
  var balloonDetected: Boolean = false

  val tower: Tower[Dart] =
    (Arrow tower) in towerPosition has values sight 1.0

  var currentDirection: Vector2D = (0.0, 0.0)
  var previousDirection: Vector2D = tower.direction

  val dummyBalloonActor: Balloon => Behavior[Update] = b =>
    Behaviors.receiveMessage {
      case UpdatePosition(replyTo) =>
        val newBalloon: Balloon =
          Simple(position = (b.position.x - 2.0, b.position.y - 2.0), boundary = balloonBoundary)
        replyTo ! BalloonMoved(newBalloon)
        dummyBalloonActor(newBalloon)
      case _ => Behaviors.same
    }

  val dummyModel: ActorRef[Update] => Behavior[Update] = t =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Step(replyTo) =>
          balloonDetected = false
          replyTo ! UpdatePosition(ctx.self)
          Behaviors.same
        case BalloonMoved(entity) =>
          t ! UpdateEntity(0.0, List(entity), ctx.self)
          Behaviors.same
        case EntityUpdated(t: Tower[Bullet], _) =>
          currentDirection = t.direction
          if (!currentDirection.equals(previousDirection)) {
            previousDirection =
              currentDirection // update direction to make this check valid on next iteration
            balloonDetected =
              true // if tower change direction means that it follow a balloon in its sight range
          }
          Behaviors.same
        case _ => Behaviors.same
      }
    }

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class TowerActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val towerActor: ActorRef[Update] = testKit.spawn(TowerActor(tower))

  val model: ActorRef[Update] = testKit.spawn(dummyModel(towerActor))

  val balloonActor: ActorRef[Update] =
    testKit.spawn(dummyBalloonActor(Simple(position = balloonPosition, boundary = balloonBoundary)))

  "The tower actor" when {
    "has just been spawned, it" should {
      "not see the balloon" in {
        model ! Step(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
    "the balloon moves through the map, it" should {
      "not see the balloon immediately, but after the second move" in {
        model ! Step(balloonActor)
        waitSomeTime()
        model ! Step(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe true
      }
    }
    "the balloon goes far away, it" should {
      "not see the balloon any more" in {
        model ! Step(balloonActor)
        waitSomeTime()
        model ! Step(balloonActor)
        waitSomeTime()
        balloonDetected shouldBe false
      }
    }
  }
}
