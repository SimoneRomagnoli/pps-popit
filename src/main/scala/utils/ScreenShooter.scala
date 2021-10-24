package utils

import utils.Constants.View.{ gameBoardHeight, gameBoardWidth }

import java.awt.{ Rectangle, Robot }
import java.io.File
import javax.imageio.ImageIO

/**
 * Utils object that takes a screenshot of the boardGame.
 */
object ScreenShooter {

  def takeScreen(x: Double, y: Double): Unit = {
    println(x + " - " + y)
    // It should take the screenshot starting from the boardgame_X and the boardgame_Y
    val rectangle =
      new Rectangle(
        x.asInstanceOf[Int],
        y.asInstanceOf[Int],
        gameBoardWidth.asInstanceOf[Int],
        gameBoardHeight.asInstanceOf[Int]
      )
    val bufferedImage = (new Robot).createScreenCapture(rectangle)
    // We should put the screen inside a proper directory
    ImageIO.write(bufferedImage, "png", new File("src/main/resources/images/tracks/track.png"))
  }

}
