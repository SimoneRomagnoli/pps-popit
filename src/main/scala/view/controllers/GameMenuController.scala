package view.controllers

import javafx.scene.Node
import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import scalafx.scene.Cursor
import scalafx.scene.control.{ Label, ToggleButton }
import scalafx.scene.layout.{ HBox, VBox }
import scalafx.scene.shape.Shape
import scalafxml.core.macros.sfxml
import view.Rendering

trait ViewGameMenuController {
  def setup(): Unit
}

@sfxml
class GameMenuController(
    val gameMenu: VBox,
    val inputButtons: HBox,
    val playButton: ToggleButton,
    val exitButton: ToggleButton,
    val gameStatus: VBox,
    val towerDepot: VBox)
    extends ViewGameMenuController {

  override def setup(): Unit = {
    setSpacing()
    setupButtons()
    setupTowerDepot()
  }

  private def setSpacing(): Unit = {
    val space: Double = 10.0
    gameMenu.setSpacing(space)
    towerDepot.setSpacing(space)
  }

  private def setupButtons(): Unit = {
    playButton.setGraphic(
      Rendering.forInput(playButton.width.value, playButton.width.value, "/images/inputs/PAUSE.png")
    )
    exitButton.setGraphic(
      Rendering.forInput(exitButton.width.value, exitButton.width.value, "/images/inputs/EXIT.png")
    )
  }

  private def setupTowerDepot(): Unit =
    TowerTypes.values.foreach { towerValue =>
      val tower: Tower[_] = towerValue.asInstanceOf[TowerType[_]].tower
      val renderedTower: Shape = Rendering a tower
      val towerBox: HBox = new HBox(renderedTower)
      towerBox.styleClass += "towerBox"
      towerBox.setCursor(Cursor.Hand)
      towerBox.onMousePressed = _ => {
        towerDepot.children.foreach(_.getStyleClass.remove("draggedOver"))
        towerBox.styleClass += "draggedOver"
        towerBox.setCursor(Cursor.ClosedHand)
      }
      towerBox.onMouseReleased = _ => towerBox.setCursor(Cursor.Hand)

      val towerLabel: Label = Label(towerValue.asInstanceOf[TowerType[_]].toString().toUpperCase)
      towerLabel.styleClass += "towerLabel"
      towerBox.children += towerLabel

      towerDepot.children.add(towerBox)
    }
}
