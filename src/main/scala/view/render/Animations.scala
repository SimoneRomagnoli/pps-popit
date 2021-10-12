package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.Explosion
import scalafx.Includes.{ at, double2DurationHelper, _ }
import scalafx.animation.Timeline
import scalafx.scene.shape.Shape

import scala.language.postfixOps

object Animations {

  sealed trait Animable
  case class Item(entity: Entity, shape: Shape) extends Animable

  case class Moving(images: Animations = Animations()) {

    def the(animable: Animable): Timeline = animable match {
      case Item(entity, shape) =>
        entity match {
          case _: Explosion =>
            new Timeline {
              autoReverse = false
              cycleCount = 1

              keyFrames = Seq(
                at(0.0 s)(shape.fill -> images.exp_1),
                at(0.1 s)(shape.fill -> images.exp_2),
                at(0.2 s)(shape.fill -> images.exp_3),
                at(0.3 s)(shape.fill -> images.exp_4),
                at(0.4 s)(shape.fill -> images.exp_5),
                at(0.5 s)(shape.fill -> images.exp_6),
                at(0.6 s)(shape.fill -> images.exp_7),
                at(0.7 s)(shape.fill -> images.exp_8)
              )
            }

        }
    }
  }

  /** Class preloading all images. */
  case class Animations(
      exp_1: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_1.png")),
      exp_2: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_2.png")),
      exp_3: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_3.png")),
      exp_4: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_4.png")),
      exp_5: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_5.png")),
      exp_6: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_6.png")),
      exp_7: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_7.png")),
      exp_8: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_8.png")))
}
