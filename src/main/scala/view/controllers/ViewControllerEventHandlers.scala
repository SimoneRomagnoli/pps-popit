package view.controllers

import controller.interaction.Messages.{ Input, Message }
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.{ PlaceTower, Selectable, Selected, TowerIn, TowerOption }
import model.maps.Cells.Cell
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.Pane
import utils.Commons
import utils.Futures.retrieve

import scala.concurrent.Future
import scala.language.implicitConversions

/**
 * Contains utility methods in order to make a more readable sequence of actions in ViewControllers
 * as handlers to input events.
 */
object ViewControllerEventHandlers {

  /** Get target [[Node]] of a [[MouseEvent]]. */
  implicit def asNode(t: EventTarget): Node = t.asInstanceOf[Node]

  /** Remove all additional effects in the scene. */
  def removeEffectsIn(pane: Pane): Unit =
    pane.children.foreach(_.setEffect(null))

  /** When hovering a cell to be selected. */
  def hoverCell(e: MouseEvent, ask: Message => Future[Message], pane: Pane): Unit = {
    val cell: Cell = Commons.Maps.gameGrid.specificCell(e.getX, e.getY)
    val effect: ColorAdjust = new ColorAdjust()
    retrieve(ask(Selectable(cell))) {
      case Selected(selectable) =>
        Platform runLater {
          removeEffectsIn(pane)
          if (selectable) {
            effect.hue = 0.12
            effect.brightness = 0.2
            e.getTarget.setCursor(Cursor.Hand)
            e.getTarget.setEffect(effect)
          } else {
            e.getTarget.setCursor(Cursor.Default)
          }
        }
      case _ =>
    }
  }

  /** When clicked a tower for more information. */
  def clickedTower(
      e: MouseEvent,
      ask: Message => Future[Message],
      fillStatus: (Tower[Bullet], Cell) => Unit): Unit = {
    val cell: Cell = Commons.Maps.gameGrid.specificCell(e.getX, e.getY)
    retrieve(ask(TowerIn(cell))) {
      case TowerOption(tower) =>
        if (tower.isDefined) Platform runLater fillStatus(tower.get, cell)
      case _ =>
    }
  }

  /** When clicked a cell to place a new tower. */
  def placeTower[B <: Bullet](
      e: MouseEvent,
      ask: Message => Future[Message],
      send: Input => Unit,
      towerType: TowerType[B]): Unit = {
    val cell: Cell = Commons.Maps.gameGrid.specificCell(e.getX, e.getY)
    retrieve(ask(Selectable(cell))) {
      case Selected(selectable) =>
        if (selectable) {
          send(PlaceTower(cell, towerType))
        }
      case _ =>
    }
  }
}
