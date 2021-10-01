package view.controllers

import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import scalafx.geometry.Pos
import scalafx.scene.Cursor
import scalafx.scene.control.{ Label, ToggleButton }
import scalafx.scene.layout.{ HBox, VBox }
import scalafx.scene.shape.Shape
import scalafxml.core.macros.sfxml
import view.render.Renders.single
import view.render.Renders.toSingle
import view.render.Rendering

trait ViewGameMenuController {
  def setup(): Unit
  def anyTowerSelected(): Boolean
  def unselectDepot(): Unit
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

  override def anyTowerSelected(): Boolean =
    towerDepot.children.map(_.getStyleClass.contains("selected")).reduce(_ || _)

  override def unselectDepot(): Unit =
    towerDepot.children.foreach(_.getStyleClass.remove("selected"))

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
      val renderedTower: Shape = Rendering a tower as single
      val towerBox: HBox = new HBox(renderedTower)
      towerBox.styleClass += "towerBox"
      towerBox.setCursor(Cursor.Hand)
      towerBox.onMousePressed = _ => {
        if (!towerBox.styleClass.contains("selected")) {
          unselectDepot()
          towerBox.styleClass += "selected"
        } else {
          unselectDepot()
        }
        towerBox.setCursor(Cursor.ClosedHand)
      }
      towerBox.onMouseReleased = _ => towerBox.setCursor(Cursor.Hand)

      val towerLabel: Label = Label(towerValue.asInstanceOf[TowerType[_]].toString().toUpperCase)
      towerLabel.styleClass += "towerLabel"
      towerBox.children += towerLabel
      towerBox.setAlignment(Pos.CenterLeft)
      towerDepot.children.add(towerBox)
    }
}
