package view.controllers

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.{ HBox, VBox }
import scalafx.scene.shape.{ Rectangle, Shape }
import scalafxml.core.macros.sfxml

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
    playButton.setGraphic(
      toInput(playButton.width.value, playButton.width.value, "/images/inputs/PAUSE.png")
    )
    exitButton.setGraphic(
      toInput(exitButton.width.value, exitButton.width.value, "/images/inputs/EXIT.png")
    )

    TowerTypes.values foreach { tower =>
      val box: HBox = new HBox()
      box.children = Seq(toShape(tower.asInstanceOf[TowerType[_]].tower))
      towerDepot.children.add(box)
    }

  }

  private def toShape(entity: Entity): Shape = {
    val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(new ImagePattern(new Image("images/" + entity.toString + ".png")))
    rectangle
  }

  private def toInput(width: Double, height: Double, path: String): Shape = {
    val rectangle: Rectangle = Rectangle(width, height)
    rectangle.setFill(new ImagePattern(new Image(path)))
    rectangle
  }
}
