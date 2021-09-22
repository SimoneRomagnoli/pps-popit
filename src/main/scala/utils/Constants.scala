package utils

import java.awt.{ GraphicsEnvironment, Toolkit }

object Constants {

  val screenWidth: Double =
    if (GraphicsEnvironment.isHeadless) 800.0 else Toolkit.getDefaultToolkit.getScreenSize.getWidth
  val widthRatio: Int = 16
  val heightRatio: Int = 9
  val width: Double = screenWidth * 3 / 4
  val height: Double = width * heightRatio / widthRatio
}
