package view.render

import javafx.scene.Node
import scalafx.collections.ObservableBuffer
import scalafx.scene.shape.Shape

import scala.language.implicitConversions

/**
 * Utility constructs for Rendering DSL.
 */
object Renders {
  sealed trait RenderMode
  case object single extends RenderMode
  case object sequence extends RenderMode

  /** Represents a container for view objects. */
  sealed trait ToBeRendered {
    def into(buffer: ObservableBuffer[Node]): Unit
    def outOf(buffer: ObservableBuffer[Node]): Unit
    def as(renderMode: RenderMode): Seq[Shape]
  }

  /** Wrapper for view-ready shapes. */
  case class Rendered(shapes: Seq[Shape]) extends ToBeRendered {
    override def into(buffer: ObservableBuffer[Node]): Unit = shapes foreach (buffer += _)

    override def outOf(buffer: ObservableBuffer[Node]): Unit = shapes foreach (buffer -= _)

    override def as(renderMode: RenderMode): Seq[Shape] = renderMode match {
      case _ => shapes
    }
  }

  implicit def toSingle(shapes: Seq[Shape]): Shape = shapes.head
  implicit def renderSingle(shape: Shape): Seq[Shape] = Seq(shape)
}
