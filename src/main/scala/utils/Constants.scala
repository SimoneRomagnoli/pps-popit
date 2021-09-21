package utils

import java.awt.{ Dimension, Toolkit }

object Constants {
  val screen: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  val widthRatio: Int = 16
  val heightRatio: Int = 9
  val width: Double = screen.getWidth * 3 / 4
  val height: Double = width * heightRatio / widthRatio
}
