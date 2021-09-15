package model

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Messages._
import model.TowersTest.{
  balloon,
  balloonPosition,
  collided,
  dummyBalloonActor,
  dummyModel,
  tower,
  towerPosition,
  waitSomeTime,
  Balloon
}
import model.actors.TowerActor
import model.tower.Position.Position
import model.tower.Tower.Tower
import org.scalatest.wordspec.AnyWordSpecLike

object TowersTest {

  val balloonPosition: Position = (6.0, 8.0)
  val towerPosition: Position = (2.0, 1.0)
  var collided: Boolean = false

  val frameRate: Double = 60.0
  val truncate: Double => Double = n => (n * 1000).round / 1000.toDouble
  val delay: Double => Double = n => truncate(1.0 / n)

  case class Balloon(var radius: Double, var position: Position) {

    def moveTo(to: Position): Balloon = {
      position = to
      this
    }
  }

  val tower: Tower = Tower(towerPosition, 1.0)
  var balloon: Balloon = Balloon(1.0, balloonPosition)

  val dummyBalloonActor: Balloon => Behavior[Update] = balloon =>
    Behaviors.receiveMessage {
      case UpdatePosition(replyTo) =>
        balloon moveTo ((balloon.position.x - 1, balloon.position.y - 2))
        replyTo ! BalloonMoved(balloon)
        Behaviors.same
      case _ => Behaviors.same
    }

  val dummyModel: ActorRef[Update] => Behavior[Update] = tower =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case Tick(replyTo) =>
          collided = false
          replyTo ! UpdatePosition(ctx.self)
          Behaviors.same
        case BalloonMoved(entity: Balloon) =>
          tower ! SearchBalloon(ctx.self, entity.position, entity.radius)
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

  val towerActor: ActorRef[Update] = testKit.spawn(TowerActor(Tower(towerPosition, 1.0)))
  val model: ActorRef[Update] = testKit.spawn(dummyModel(towerActor))

  val balloonActor: ActorRef[Update] =
    testKit.spawn(dummyBalloonActor(Balloon(1.0, balloonPosition)))

  "During local running" when {
    "tower and balloon has just spawned" should {
      "be across the map and not collide" in {
        tower.position shouldBe towerPosition
        balloon.position shouldBe balloonPosition
        tower collidesWith (balloon.position, balloon.radius) shouldBe false
      }
    }
    "balloon goes near the tower" should {
      "be in tower sight range and collide" in {
        balloon moveTo ((3.0, 2.0))
        tower.position shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower collidesWith (balloon.position, balloon.radius) shouldBe true
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
