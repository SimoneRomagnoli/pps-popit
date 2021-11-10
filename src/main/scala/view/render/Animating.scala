package view.render

import javafx.scene.Node
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ CannonBall, IceBall }
import scalafx.animation.Timeline
import scalafx.collections.ObservableBuffer
import scalafx.scene.shape.{ Rectangle, Shape }
import view.render.Animations.{ Item, Moving }

import scala.language.{ implicitConversions, reflectiveCalls }

/**
 * Object that simulates a DSL for animating logic entities as timelines.
 */
object Animating {

  /** Represents an animable object */
  trait Animable {
    def into(buffer: ObservableBuffer[Node]): Unit
  }

  /** Represents an animated shape by a [[Timeline]] */
  case class Animated(shape: Shape, timeline: Timeline) extends Animable {

    override def into(buffer: ObservableBuffer[Node]): Unit = {
      buffer += shape
      timeline.onFinished = _ => buffer -= shape
      timeline.play()
    }
  }

  val moving: Moving = Moving()

  /**
   * Allows to turn a generic element into a [[Animable]]. It is necessarily contravariant because
   * of the entities' hierarchy: an entities animator should be able to animate any [[Entity]]
   * sub-type.
   *
   * @tparam T,
   *   the type of the element to be animated.
   */
  trait Animator[-T] {
    def animate(elem: T): Animable
  }

  /**
   * Animate a generic element in a [[Shape]] and gives the possibility to put it in a ScalaFX node.
   *
   * @param elem,
   *   the generic element to be animated
   * @param animator,
   *   the implicit [[Animator]] that defines how the animation happens
   * @tparam T,
   *   the type of the element to be animated
   * @return
   *   an [[Animable]] element
   */
  def an[T](elem: T)(implicit animator: Animator[T]): Animable = animator animate elem

  /** Contains all the Animators */
  object Animators {

    /** Animates an [[Entity]] */
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
