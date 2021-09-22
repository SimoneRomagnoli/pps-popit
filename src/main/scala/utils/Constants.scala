package utils

import model.Positions.Vector2D

import java.awt.{ Dimension, Toolkit }

object Constants {
  val screen: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  val widthRatio: Int = 16
  val heightRatio: Int = 9
  val width: Double = screen.getWidth * 3 / 4
  val height: Double = width * heightRatio / widthRatio

  val position: Vector2D = (0.0, 0.0)
  val sightRange: Double = 1.0
  val shotRatio: Double = 0.5
  val speed: Vector2D = (1.0, 1.0)
  val damage: Double = 1.0
  val radius: Double = 2.0
  val freezingTime: Double = 1.0
  val bulletBoundary: (Double, Double) = (2.0, 1.0)
  val balloonBoundary: Double = 1.0
}
