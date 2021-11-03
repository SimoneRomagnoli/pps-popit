package controller.inout

import commons.CommonValues.View.{ gameBoardHeight, gameBoardWidth }
import controller.inout.FileCoders.CoderBuilder._

import java.awt.{ Rectangle, Robot }
import java.io.File
import javax.imageio.ImageIO

/**
 * Utils object that takes a screenshot of the boardGame to create the SavedTracksPage.
 */
object ScreenShooter {

  def takeScreen(x: Double, y: Double, index: Int): Unit = {
    val rectangle =
      new Rectangle(x.toInt, y.toInt, gameBoardWidth.toInt, gameBoardHeight.toInt)
    val bufferedImage = (new Robot).createScreenCapture(rectangle)
    ImageIO.write(bufferedImage, "png", new File(imagesDir + separator + "track" + index + ".png"))
  }

}
