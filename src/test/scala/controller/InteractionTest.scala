package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.ActorRef
import controller.Controller.ControllerActor
import controller.Messages.{ Input, Render }
import org.scalatest.wordspec.AnyWordSpecLike

object InteractionTest {}

class InteractionTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val controller: ActorRef[Input] = testKit.spawn(ControllerActor(view.ref))

}
