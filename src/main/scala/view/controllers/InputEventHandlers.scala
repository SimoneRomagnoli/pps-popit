package view.controllers

import cats.effect.IO
import javafx.event.EventTarget
import javafx.scene.Node
import model.maps.Cells.Cell
import scalafx.scene.Cursor
import scalafx.scene.effect.ColorAdjust
import javafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane
import utils.Constants

import scala.language.implicitConversions

/**
 * Contains utility methods that exploit [[IO]] in order to make a more readable sequence of actions
 * in ViewControllers as handlers to input events.
 */
object InputEventHandlers {

  /** Implicit conversion for Unit monad. */
  implicit def unitToIO(exp: => Unit): IO[Unit] = IO(exp)

  /** Get target [[Node]] of a [[MouseEvent]]. */
  implicit def asNode(t: EventTarget): Node = t.asInstanceOf[Node]

  def removeEffectsIn(pane: Pane): IO[Unit] =
    pane.children.foreach(_.setEffect(null))

  def hoverCell(e: MouseEvent, occupiedCells: Seq[Cell]): IO[Unit] = {
    val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
    val effect: ColorAdjust = new ColorAdjust()
    if (selectable(occupiedCells, cell)) {
      effect.hue = 0.12
      effect.brightness = 0.2
      e.getTarget.setCursor(Cursor.Hand)
    } else {
      e.getTarget.setCursor(Cursor.Default)
    }
    e.getTarget.setEffect(effect)
  }

  def hoverTower(e: MouseEvent): IO[Unit] =
    if (e.getTarget.getStyleClass.contains("tower")) {
      val effect: ColorAdjust = new ColorAdjust()
      effect.hue = 0.12
      effect.brightness = 0.2
      e.getTarget.setCursor(Cursor.Hand)
    } else IO.unit

  private def selectable(occupiedCells: Seq[Cell], cell: Cell): Boolean =
    !occupiedCells.exists(c => c.x == cell.x && c.y == cell.y)

}
