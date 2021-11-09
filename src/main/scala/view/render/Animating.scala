package view.render

import javafx.scene.Node
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ CannonBall, IceBall }
import scalafx.animation.Timeline
import scalafx.collections.ObservableBuffer
import scalafx.scene.shape.{ Rectangle, Shape }
import view.render.Animations.{ Item, Moving }

import scala.language.{ implicitConversions, reflectiveCalls }

object Animating {

  trait Animable {
    def into(buffer: ObservableBuffer[Node]): Unit
  }

  case class Animated(shape: Shape, timeline: Timeline) extends Animable {

    override def into(buffer: ObservableBuffer[Node]): Unit = {
      buffer += shape
      timeline.onFinished = _ => buffer -= shape
      timeline.play()
    }
  }

  val moving: Moving = Moving()

  trait Animator[-T] {
    def animate(elem: T): Animable
  }

  def an[T](elem: T)(implicit animator: Animator[T]): Animable = animator animate elem

  object Animators {

    implicit val entityAnimator: Animator[Entity] = (entity: Entity) => {
      val rectangle: Rectangle = Rectangle(
        entity.position.x - (entity.boundary._1 * 2),
        entity.position.y - (entity.boundary._2 * 2),
        entity.boundary._2 * 4,
        entity.boundary._2 * 4
      )

      val timeline: Timeline = entity match {
        case cannonBall: CannonBall => moving the Item(cannonBall, rectangle)
        case iceBall: IceBall       => moving the Item(iceBall, rectangle)
      }

      Animated(rectangle, timeline)
    }
  }
}
