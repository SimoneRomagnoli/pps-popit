package model.actors

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import controller.interaction.GameLoop.GameLoopActor
import controller.interaction.GameLoop.GameLoopMessages.{ModelUpdated, Start}
import controller.interaction.Messages.{Input, Render, Update}
import controller.settings.Settings.Time.TimeSettings
import model.Model.ModelMessages.TickUpdate
import model.Positions.defaultPosition
import model.actors.BulletActorTest.{dart, dummyModel}
import model.entities.balloons.BalloonLives.Blue
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{Bullet, Dart}
import model.managers.EntitiesMessages.{EntityUpdated, UpdateEntity}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import view.View.ViewMessages.RenderEntities

import scala.language.postfixOps

object BulletActorTest {
  var gameLoop: Option[ActorRef[Input]] = None
  var dart: Bullet = Dart() in (100.0, 100.0)
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
  val gameLoop: ActorRef[Input] = testKit.spawn(GameLoopActor(model, view.ref, TimeSettings()))

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
}
