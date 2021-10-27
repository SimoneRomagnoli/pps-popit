package model.actors

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import controller.GameLoop.GameLoopActor
import controller.GameLoop.GameLoopMessages.{ ModelUpdated, Start }
import controller.Messages.{ Input, Render, Update }
import model.Model.ModelMessages.TickUpdate
import model.Positions.defaultPosition
import model.actors.BulletActorTest.{ balloon, dart, dummyModel }
import model.entities.balloons.BalloonLives.{ Blue, Red }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ Bullet, Dart }
import model.managers.EntitiesMessages.{ EntityUpdated, UpdateEntity }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import view.View.ViewMessages.RenderEntities

import scala.language.postfixOps

object BulletActorTest {
  var gameLoop: Option[ActorRef[Input]] = None
  var dart: Bullet = Dart() in (100.0, 100.0)
  //var explosion: Explosion = CannonBall(bulletDefaultRadius)
  var balloon: Balloon = (Blue balloon) in (0.0, 0.0)

  val dummyModel: ActorRef[Update] => Behavior[Update] = b =>
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case TickUpdate(elapsedTime, replyTo) =>
          gameLoop = Some(replyTo)
          b ! UpdateEntity(elapsedTime, List(dart, balloon), ctx.self)
          Behaviors.same
        case EntityUpdated(entity, _) =>
          dart = entity.asInstanceOf[Dart]
          gameLoop.get ! ModelUpdated(List(), List())
          Behaviors.same
        case _ => Behaviors.same
      }
    }
}

class BulletActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with Matchers {
  val instance: Balloon => Balloon = b => b
  val bulletActor: ActorRef[Update] = testKit.spawn(BulletActor(dart))

  val model: ActorRef[Update] =
    testKit.spawn(dummyModel(bulletActor))

  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val gameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(model, view.ref))

  "The bullet actor" when {
    "asked to update" should {
      "reply to the model which should contact the view" in {
        gameLoop ! Start()
        view expectMessage RenderEntities(List())
      }
      "update the position of its bullet" in {
        (dart position) should be !== defaultPosition
      }
    }
  }

//  "The bullet actor" when {
//    "a balloon exits from the screen" should {
//      "send to the model a message before die" in {
//        //dart.update(-1, -1)
//        model
//      }
//    }
//  }

//  "The bullet actor" when {
//    "the dart touch a balloon" should {
//      "reply to the model which should contact the view" in {
//        println(balloon position)
//        gameLoop ! Start()
//      }
//      "hit the balloon" in {
//        println(dart position)
//        println(balloon position)
//        (dart hit balloon) shouldBe true
//        model expectMessage
//      }
//    }
//  }

}
