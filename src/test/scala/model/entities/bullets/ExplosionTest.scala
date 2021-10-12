package model.entities.bullets

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import scalafx.Includes.{ at, double2DurationHelper, _ }
import scalafx.animation.{ KeyValue, Timeline }
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.shape.Rectangle

import scala.language.postfixOps

object ExplosionTest extends JFXApp3 {

  override def start(): Unit = {

    val pane: Pane = new Pane()

    stage = new PrimaryStage() {

      scene = new Scene(pane)
    }

    val rec1: Rectangle = Rectangle(100, 100, 100, 100)

    val exp_1: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_1.png"))
    val exp_2: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_2.png"))
    val exp_3: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_3.png"))
    val exp_4: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_4.png"))
    val exp_5: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_5.png"))
    val exp_6: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_6.png"))
    val exp_7: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_7.png"))
    val exp_8: ImagePattern = new ImagePattern(new Image("images/bullets/EXPLOSION_8.png"))

    val timeline: Timeline = new Timeline {
      autoReverse = false
      cycleCount = Timeline.Indefinite
      keyFrames = Seq(
        at(0.0 s)(rec1.fill -> exp_1),
        at(0.1 s)(rec1.fill -> exp_2),
        at(0.2 s)(rec1.fill -> exp_3),
        at(0.3 s)(rec1.fill -> exp_4),
        at(0.4 s)(rec1.fill -> exp_5),
        at(0.5 s)(rec1.fill -> exp_6),
        at(0.6 s)(rec1.fill -> exp_7),
        at(0.7 s)(rec1.fill -> exp_8)
      )
    }
    pane.children += rec1
    timeline.play()
  }
}
