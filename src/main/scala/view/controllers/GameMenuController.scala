package view.controllers

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import scalafx.scene.Parent
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, Pane, VBox }
import scalafx.scene.shape.{ Rectangle, Shape }
import scalafxml.core.macros.sfxml
import utils.Constants.View.{ gameMenuHeight, gameMenuWidth }

trait ViewGameMenuController {
  def setup(): Unit
}

@sfxml
class GameMenuController(
    val gameMenu: VBox,
    val inputButtons: HBox,
    val playButton: ToggleButton,
    val exitButton: ToggleButton,
    val gameStatus: HBox,
    val towerDepot: VBox)
    extends ViewGameMenuController {

  override def setup(): Unit = {
    setLayout(gameMenu, gameMenuWidth, gameMenuHeight)
    playButton.setGraphic(
      toInput(playButton.width.value, playButton.width.value, "/images/inputs/PAUSE.png")
    )
    exitButton.setGraphic(
      toInput(exitButton.width.value, exitButton.width.value, "/images/inputs/EXIT.png")
    )
  }

  private def toInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }

  private def setLayout(parent: Parent, width: Double, height: Double): Unit = {
    parent.maxWidth(width)
    parent.minWidth(width)
    parent.maxHeight(height)
    parent.minHeight(height)
  }
}
