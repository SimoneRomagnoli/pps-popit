package utils

import model.Positions.Vector2D
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.TowerTypes.{ Arrow, Cannon, Ice }
import model.entities.towers.Towers.Tower
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import utils.Constants.Screen.{ cellSize, height, heightRatio, width, widthRatio }
import utils.Constants.View.gameMenuWidthRatio

import java.awt.{ GraphicsEnvironment, Toolkit }
import scala.language.postfixOps

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

      object TowerTypes {
        val towerDefaultCost: Int = 200
        val arrow: Tower[Dart] = Arrow tower
        val cannon: Tower[CannonBall] = Cannon tower
        val ice: Tower[IceBall] = Ice tower
      }

      object TowerPowerUps {
        val boostedRatioCost: Int = 200
        val boostedRatioFactor: Double = 2.0
        val boostedSightCost: Int = 200
        val boostedSightFactor: Double = 2.0
        val boostedCamoCost: Int = 200
        val boostedDamageCost: Int = 200
        val boostedDamageFactor: Double = 2.0
      }
    }

    object Bullets {
      val bulletDefaultBoundary: (Double, Double) = (cellSize / 4, cellSize / 4)
      val bulletDefaultDamage: Double = 1.0
      val bulletDefaultRadius: Double = cellSize / 4
      val bulletFreezingTime: Double = 1.0
      val bulletDefaultSpeed: Vector2D = (100.0, -100.0)
      val bulletSpeedFactor: Double = 1000.0
    }
  }

  object Maps {
    val outerCell: Cell = GridCell(-1, -1)
    val basicTrack: Seq[Cell] = for (x <- 0 until Screen.widthRatio) yield GridCell(x, 0, RIGHT)
    val gameGrid: Grid = Grid(widthRatio - gameMenuWidthRatio, heightRatio)
  }

  object View {
    val gameMenuWidthRatio: Int = 3
    val gameMenuWidth: Double = cellSize * gameMenuWidthRatio
    val gameMenuHeight: Double = height
    val gameBoardWidth: Double = width - gameMenuWidth
    val gameBoardHeight: Double = height

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
