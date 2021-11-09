package view.render

import javafx.scene.Node
import scalafx.collections.ObservableBuffer
import scalafx.scene.shape.Shape

import scala.language.implicitConversions

/**
 * Utility constructs for Rendering DSL.
 */
object Renders {

  /** Represents a container for view objects. */
  sealed trait Renderable {
    def shapes: Seq[Shape]
    def into(buffer: ObservableBuffer[Node]): Unit
    def asSingle: Shape = shapes.head
  }

  /** Wrapper for view-ready shapes. */
  case class Rendered(override val shapes: Seq[Shape]) extends Renderable {
    override def into(buffer: ObservableBuffer[Node]): Unit = shapes foreach (buffer += _)
  }

  implicit def renderSingle(shape: Shape): Seq[Shape] = Seq(shape)
}
