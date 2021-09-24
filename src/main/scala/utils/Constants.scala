package utils

import java.awt.{ GraphicsEnvironment, Toolkit }
import model.Positions.Vector2D
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.RIGHT

object Constants {

  val screenWidth: Double =
    if (GraphicsEnvironment.isHeadless) 800.0 else Toolkit.getDefaultToolkit.getScreenSize.getWidth
  val widthRatio: Int = 16
  val heightRatio: Int = 9
  val width: Double = screenWidth * 3 / 4
  val height: Double = width * heightRatio / widthRatio

  val position: Vector2D = (0.0, 0.0)
  val sightRange: Double = 1.0
  val shotRatio: Double = 0.5
  val towerDefaultBoundary: (Double, Double) = (40.0, 40.0)
  val towerDefaultDirection: Vector2D = (0.0, 0.0)
  val speed: Vector2D = (1.0, 1.0)
  val damage: Double = 1.0
  val radius: Double = 2.0
  val freezingTime: Double = 1.0
  val bulletBoundary: (Double, Double) = (2.0, 1.0)
  val balloonBoundary: Double = 1.0

  val basicTrack: Seq[Cell] = for (x <- 0 until widthRatio) yield GridCell(x, 0, RIGHT)

  val loadingLabels: List[String] = List(
    "The monkeys are teaming up...",
    "We are building the towers...",
    "We are blowing the balloons...",
    "We are cleaning the roads...",
    "We are recharging the bullets...",
    "Your wallet is being prepared..."
  )
}
