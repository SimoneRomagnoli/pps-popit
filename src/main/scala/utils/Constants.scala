package utils

import model.Positions.Vector2D
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Tracks.Directions.RIGHT
import utils.Constants.Screen.cellSize

import java.awt.{ GraphicsEnvironment, Toolkit }

object Constants {

  object Screen {

    val screenWidth: Double =
      if (GraphicsEnvironment.isHeadless) 800.0
      else Toolkit.getDefaultToolkit.getScreenSize.getWidth
    val widthRatio: Int = 16
    val heightRatio: Int = 9
    val width: Double = screenWidth * 3 / 4
    val height: Double = width * heightRatio / widthRatio
    val cellSize: Double = width / widthRatio
  }

  object Entities {
    val defaultPosition: Vector2D = (0.0, 0.0)

    object Balloons {
      val balloonDefaultBoundary: (Double, Double) = (cellSize / 3, cellSize / 2)
      val balloonDefaultSpeed: Vector2D = (1.0, 1.0)
    }

    object Towers {
      val towerDefaultSightRange: Double = cellSize * 3 / 2
      val towerDefaultShotRatio: Double = 0.5
      val towerDefaultBoundary: (Double, Double) = (cellSize / 2, cellSize / 2)
      val towerDefaultDirection: Vector2D = (0.0, 0.0)
    }

    object Bullets {
      val bulletDefaultBoundary: (Double, Double) = (2.0, 1.0)
      val bulletDefaultDamage: Double = 1.0
      val bulletDefaultRadius: Double = 2.0
      val bulletFreezingTime: Double = 1.0
      val bulletDefaultSpeed: Vector2D = (1.0, 1.0)
    }
  }

  object Maps {
    val basicTrack: Seq[Cell] = for (x <- 0 until Screen.widthRatio) yield GridCell(x, 0, RIGHT)
  }

  object View {

    val loadingLabels: List[String] = List(
      "The monkeys are teaming up...",
      "We are building the towers...",
      "We are blowing the balloons...",
      "We are cleaning the roads...",
      "We are recharging the bullets...",
      "Your wallet is being prepared..."
    )
  }
}
