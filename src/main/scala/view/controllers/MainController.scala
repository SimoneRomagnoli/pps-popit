package view.controllers

import scalafx.scene.layout.BorderPane
import scalafxml.core.macros.{ nested, sfxml }
import utils.Constants

trait ViewController {
  def gameController: ViewGameController
}

@sfxml
class MainController(
    val mainPane: BorderPane,
    val game: BorderPane,
    @nested[GameController] val gameController: ViewGameController)
    extends ViewController {
  mainPane.maxWidth = Constants.Screen.width
  mainPane.minWidth = Constants.Screen.width
  mainPane.maxHeight = Constants.Screen.height
  mainPane.minHeight = Constants.Screen.height

}
