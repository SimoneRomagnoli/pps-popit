package view.render

import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ Bullet, Explosion }
import model.entities.towers.Towers.Tower
import scalafx.animation.Timeline
import scalafx.scene.layout.Pane
import scalafx.scene.shape.Rectangle
import view.render.Animations2.Composing
import view.render.Animations2.Item
import view.render.Renders.{ renderSingle, Rendered, ToBeRendered }

import scala.language.{ implicitConversions, reflectiveCalls }

object Animating {

  val composing: Composing = Composing()

  def in(entity: Entity, pane: Pane): Timeline = {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      300,
      300
    )

    val timeline: Timeline = entity match {
      case explosion: Explosion => composing the Item(explosion, rectangle)
    }
    Drawings into pane.children
    timeline
  }
}
