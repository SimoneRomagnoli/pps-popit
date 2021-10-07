package view.controllers

import cats.effect.IO
import controller.Messages.{ Message, TowerIn, TowerOption }
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import model.entities.towers.Towers.Tower
import model.maps.Cells.Cell
import scalafx.scene.Cursor
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.Pane
import utils.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{ Failure, Success }

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

  def clickedTower(
      e: MouseEvent,
      ask: Message => Future[Message],
      fillStatus: (Tower[_], Cell) => Unit): IO[Unit] = {
    val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
    ask(TowerIn(cell)).onComplete {
      case Success(value) =>
        value.asInstanceOf[TowerOption] match {
          case TowerOption(tower) =>
            tower match {
              case Some(tower) => fillStatus(tower, cell)
              case _           =>
            }
        }
      case Failure(exception) => println(exception)
    }
    if (e.getTarget.getStyleClass.contains("tower")) {
      val effect: ColorAdjust = new ColorAdjust()
      effect.hue = 0.12
      effect.brightness = 0.2
      e.getTarget.setEffect(effect)
      e.getTarget.setCursor(Cursor.Hand)
    }
  }

  private def selectable(occupiedCells: Seq[Cell], cell: Cell): Boolean =
    !occupiedCells.exists(c => c.x == cell.x && c.y == cell.y)

}
