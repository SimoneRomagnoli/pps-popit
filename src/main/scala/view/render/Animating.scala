package view.render

import javafx.scene.Node
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ CannonBall, Explosion, IceBall }
import scalafx.animation.Timeline
import scalafx.collections.ObservableBuffer
import scalafx.scene.shape.{ Rectangle, Shape }
import view.render.Animations.{ Item, Moving }

import scala.language.{ implicitConversions, reflectiveCalls }

object Animating {

  trait ToBeAnimated {
    def into(buffer: ObservableBuffer[Node]): Unit
  }

  case class Animated(shape: Shape, timeline: Timeline) extends ToBeAnimated {

    override def into(buffer: ObservableBuffer[Node]): Unit = {
      buffer += shape
      timeline.onFinished = _ => buffer -= shape
      timeline.play()
    }
  }

  val moving: Moving = Moving()

  def an(entity: Entity): ToBeAnimated = {
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
