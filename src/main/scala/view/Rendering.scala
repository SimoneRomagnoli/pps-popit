package view

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import scalafx.scene.shape.{ Rectangle, Shape }

object Rendering {

  def a(entity: Entity): Shape = {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(new ImagePattern(new Image("images/" + entity.toString + ".png")))
    rectangle
  }

  def forInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

}
