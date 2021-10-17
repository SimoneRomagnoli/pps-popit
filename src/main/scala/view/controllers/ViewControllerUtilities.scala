package view.controllers

import controller.Controller.ControllerMessages.{ ActorInteraction, PlaceTower }
import controller.Messages.{ Input, Message }
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.{ Selectable, Selected, TowerIn, TowerOption }
import model.maps.Cells.Cell
import scalafx.application.Platform
import scalafx.scene.Cursor
import scalafx.scene.effect.ColorAdjust
import scalafx.scene.layout.Pane
import utils.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.{ Failure, Success }

/**
 * Contains utility methods in order to make a more readable sequence of actions in ViewControllers
 * as handlers to input events.
 */
object ViewControllerUtilities {

  /** Get target [[Node]] of a [[MouseEvent]]. */
  implicit def asNode(t: EventTarget): Node = t.asInstanceOf[Node]

  /** Remove all additional effects in the scene. */
  def removeEffectsIn(pane: Pane): Unit =
    pane.children.foreach(_.setEffect(null))

  /** When hovering a cell to be selected. */
  def hoverCell(e: MouseEvent, ask: Message => Future[Message], pane: Pane): Unit = {
    val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
    val effect: ColorAdjust = new ColorAdjust()
    ask(Selectable(cell)) onComplete {
      case Failure(_) =>
      case Success(value) =>
        value match {
          case ActorInteraction(_, _) =>
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
        }
    }

  }

  /** When clicked a tower for more information. */
  def clickedTower(
      e: MouseEvent,
      ask: Message => Future[Message],
      fillStatus: (Tower[Bullet], Cell) => Unit): Unit = {
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

  /** When clicked a cell to place a new tower. */
  def placeTower(
      e: MouseEvent,
      ask: Message => Future[Message],
      send: Input => Unit,
      menu: ViewGameMenuController): Unit = {
    val cell: Cell = Constants.Maps.gameGrid.specificCell(e.getX, e.getY)
    ask(Selectable(cell)).onComplete {
      case Success(value) =>
        value match {
          case Selected(selectable) =>
            if (selectable) {
              Platform runLater {
                menu.unselectDepot()
                send(PlaceTower(cell, menu.getSelectedTowerType))
              }
            }
        }
      case Failure(exception) =>
    }
  }

  private def andThen(implicit future: Future[Message], handler: Message => Unit): Unit =
    future onComplete {
      case Failure(_) =>
      case Success(value) =>
        value match {
          case ActorInteraction(_, _) =>
          case msg                    => handler(msg)
        }
    }
}
