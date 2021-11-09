package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ CannonBall, IceBall }
import scalafx.Includes.{ at, double2DurationHelper, _ }
import scalafx.animation.Timeline
import scalafx.scene.shape.Shape

import scala.language.postfixOps

object Animations {

  sealed trait Animation
  case class Item(entity: Entity, shape: Shape) extends Animation

  case class Moving(images: Animations = Animations()) {

    def the(animation: Animation): Timeline = animation match {
      case Item(entity, shape) =>
        entity match {
          case _: CannonBall =>
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

          case _: IceBall =>
            new Timeline {
              autoReverse = false
              cycleCount = 1

              keyFrames = Seq(
                at(0.0 s)(shape.fill -> images.ice_exp_1),
                at(0.08 s)(shape.fill -> images.ice_exp_2),
                at(0.16 s)(shape.fill -> images.ice_exp_3),
                at(0.32 s)(shape.fill -> images.ice_exp_4),
                at(0.40 s)(shape.fill -> images.ice_exp_5),
                at(0.48 s)(shape.fill -> images.ice_exp_6),
                at(0.56 s)(shape.fill -> images.ice_exp_7),
                at(0.64 s)(shape.fill -> images.ice_exp_8),
                at(0.72 s)(shape.fill -> images.ice_exp_9),
                at(0.80 s)(shape.fill -> images.ice_exp_10),
                at(0.88 s)(shape.fill -> images.ice_exp_11)
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
      exp_8: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_8.png")),
      ice_exp_1: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_1.png")),
      ice_exp_2: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_2.png")),
      ice_exp_3: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_3.png")),
      ice_exp_4: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_4.png")),
      ice_exp_5: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_5.png")),
      ice_exp_6: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_6.png")),
      ice_exp_7: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_7.png")),
      ice_exp_8: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_8.png")),
      ice_exp_9: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_9.png")),
      ice_exp_10: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_10.png")),
      ice_exp_11: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-EXPLOSION_11.png")))
}
