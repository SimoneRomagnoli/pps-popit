package utils

import utils.Commons.View.{ gameBoardHeight, gameBoardWidth }
import java.awt.{ Rectangle, Robot }
import java.io.File
import javax.imageio.ImageIO

/**
 * Utils object that takes a screenshot of the boardGame.
 */
object ScreenShooter {

  def takeScreen(x: Double, y: Double, index: Int): Unit = {
    val rectangle =
      new Rectangle(x.toInt, y.toInt, gameBoardWidth.toInt, gameBoardHeight.toInt)
    val bufferedImage = (new Robot).createScreenCapture(rectangle)
    ImageIO.write(
      bufferedImage,
      "png",
      new File("src/main/resources/images/tracks/track" + index + ".png")
    )
  }

}
