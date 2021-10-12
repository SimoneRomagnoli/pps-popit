package view.animations

import scalafx.animation.KeyFrame
import scalafx.scene.image.ImageView

object Animations {

  trait EntityAnimation {

    /** Type annotation for a Seq of KeyFrames */
    type AnimationFrames = Seq[KeyFrame]

    /** The image of the animal displayed in the GUI */
    val imageView: ImageView

    /** Starts the animation */
    def play(): Unit

    /** Stops the animation */
    def stop(): Unit
  }

  case class CannonBallExplosion() extends EntityAnimation {

    /** The image of the animal displayed in the GUI */
    override val imageView: ImageView = ???

    /** Starts the animation */
    override def play(): Unit = ???

    /** Stops the animation */
    override def stop(): Unit = ???
  }

}
