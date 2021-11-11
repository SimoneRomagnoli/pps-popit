package commons

import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.Right
import commons.CommonValues.Screen._
import commons.CommonValues.View.gameMenuWidthRatio

import java.awt.{ GraphicsEnvironment, Toolkit }
import scala.language.postfixOps

object CommonValues {

  object Resources {
    val akkaConfiguration: String = "akka/application.conf"
    val fxmlRoot: String = "/fxml/root.fxml"
  }

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

  object Maps {
    val outerCell: Cell = GridCell(-1, -1)
    val basicTrack: Seq[Cell] = for (x <- 0 until Screen.widthRatio) yield GridCell(x, 0, Right)
    val gameGrid: Grid = Grid(widthRatio - gameMenuWidthRatio, heightRatio)
  }

  object View {
    val gameMenuWidthRatio: Int = 3
    val gameBoardWidthRatio: Int = widthRatio - gameMenuWidthRatio
    val gameMenuWidth: Double = cellSize * gameMenuWidthRatio
    val gameMenuHeight: Double = height
    val gameBoardWidth: Double = width - gameMenuWidth
    val gameBoardHeight: Double = height
  }

  object Game {
    val balloonHitGain: Int = 30
  }
}
