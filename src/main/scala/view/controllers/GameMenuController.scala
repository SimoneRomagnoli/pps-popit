package view.controllers

import controller.Messages
import controller.Messages.{
  BoostTowerIn,
  Input,
  Message,
  PauseGame,
  ResumeGame,
  TowerIn,
  TowerOption
}
import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
import model.entities.towers.towerpowerups.TowerUpgrades.{ Camo, PowerUp, Ratio, Sight }
import model.maps.Cells.Cell
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Cursor
import scalafx.scene.control.{ Label, ToggleButton }
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout._
import scalafx.scene.shape.Shape
import scalafxml.core.macros.sfxml
import utils.Constants.Maps.outerCell
import view.render.Rendering
import view.render.Renders.{ single, toSingle }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait ViewGameMenuController extends ViewController {
  def setup(): Unit
  def setHighlightingTower(reference: (Tower[_], Boolean) => Unit): Unit
  def update(stats: GameStats): Unit
  def anyTowerSelected(): Boolean
  def unselectDepot(): Unit
  def fillTowerStatus(tower: Tower[Bullet], cell: Cell): Unit
  def clearTowerStatus(): Unit
  def isPaused: Boolean
  def getSelectedTowerType[B <: Bullet]: TowerType[B]
}

/**
 * Controller class bound to the game menu fxml.
 */
@sfxml
class GameMenuController(
    val gameMenu: VBox,
    val playButton: ToggleButton,
    val exitButton: ToggleButton,
    val gameStatus: VBox,
    val statusUpperBox: HBox,
    val lifeLabel: Label,
    val statusLowerBox: HBox,
    val moneyLabel: Label,
    val towerDepot: VBox,
    val towerStatus: VBox,
    var currentCell: Cell = outerCell,
    var send: Input => Unit,
    var ask: Message => Future[Message],
    var highlight: (Tower[_], Boolean) => Unit,
    var paused: Boolean = false,
    var selectedTowerType: TowerType[_])
    extends ViewGameMenuController {
  import MenuSetters._

  override def setup(): Unit = {
    setSpacing()
    setupButtons()
    setupTowerDepot()
  }

  override def setSend(reference: Messages.Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference

  override def setHighlightingTower(reference: (Tower[_], Boolean) => Unit): Unit = highlight =
    reference

  override def isPaused: Boolean = paused

  override def anyTowerSelected(): Boolean =
    towerDepot.children.map(_.getStyleClass.contains("selected")).reduce(_ || _)

  override def unselectDepot(): Unit =
    towerDepot.children.foreach(_.getStyleClass.remove("selected"))

  override def getSelectedTowerType[B <: Bullet]: TowerType[B] =
    selectedTowerType.asInstanceOf[TowerType[B]]

  override def update(stats: GameStats): Unit = {
    lifeLabel.text = stats.life.toString
    moneyLabel.text = stats.wallet.toString
  }

  override def fillTowerStatus(tower: Tower[Bullet], cell: Cell): Unit = Platform runLater {
    clearTowerStatus()
    if (currentCell == cell) {
      currentCell = outerCell
      highlight(tower, false)
    } else {
      currentCell = cell
      addToTowerStatus(Rendering a tower as single)
      addToTowerStatus("Sight Range", tower.sightRange, Sight)
      addToTowerStatus("Shot Ratio", tower.shotRatio, Ratio)
      //addToTowerStatus("Damage", tower.bullet.damage, Damage)
      addToTowerStatus(
        "Camo Vision",
        if (tower.isInstanceOf[EnhancedSightAbility]) "Yes" else "No",
        Camo
      )
      highlight(tower, true)
    }
  }

  override def clearTowerStatus(): Unit =
    towerStatus.children.clear()

  /** Private methods for setting the controller. */
  private object MenuSetters {

    def setSpacing(): Unit = {
      val space: Double = 10.0
      gameMenu.setSpacing(space)
      towerDepot.setSpacing(space)
      statusUpperBox.setAlignment(Pos.CenterLeft)
      statusLowerBox.setAlignment(Pos.CenterLeft)
    }

    def setupButtons(): Unit =
      //exitButton.onMouseClicked = _ =>
      playButton.onMouseClicked = _ =>
        if (paused) {
          send(ResumeGame())
          paused = false
        } else {
          send(PauseGame())
          paused = true
        }

    def setupTowerDepot[B <: Bullet](): Unit =
      TowerTypes.values.foreach { towerValue =>
        val tower: Tower[B] = towerValue.asInstanceOf[TowerType[B]].tower
        val renderedTower: Shape = Rendering a tower as single
        val towerBox: HBox = new HBox(renderedTower)
        towerBox.styleClass += "towerBox"
        towerBox.setCursor(Cursor.Hand)
        towerBox.onMousePressed = _ =>
          if (!paused) {
            if (!towerBox.styleClass.contains("selected")) {
              unselectDepot()
              towerBox.styleClass += "selected"
              selectedTowerType = towerValue.asInstanceOf[TowerType[B]]
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

    def addToTowerStatus(shape: Shape): Unit = {
      val box: HBox = new HBox()
      box.children += shape
      box.styleClass += "towerStatusImage"
      box.setAlignment(Pos.Center)
      towerStatus.children += box
    }

    def addToTowerStatus[T](title: String, argument: T, powerUp: PowerUp): Unit = {
      val box: HBox = new HBox()
      val key: Label = Label(title + ": ")
      val value: Label = Label(argument.toString)
      val emptyBox: HBox = new HBox()
      emptyBox.hgrow = Always
      val button: ToggleButton = new ToggleButton(powerUp.cost.toString)
      button.onMouseClicked = _ => {
        send(BoostTowerIn(currentCell, powerUp))
        refreshTowerStatus()
      }
      button.styleClass += "inputButton"
      box.children += key
      box.children += value
      box.children += emptyBox
      box.children += button
      box.styleClass += "towerStatusBox"
      box.setAlignment(Pos.CenterLeft)
      towerStatus.children += box
    }

    def refreshTowerStatus(): Unit = Platform runLater {
      ask(TowerIn(currentCell)) onComplete {
        case Failure(exception) => println(exception)
        case Success(value) =>
          value.asInstanceOf[TowerOption] match {
            case TowerOption(option) =>
              option match {
                case Some(tower) =>
                  fillTowerStatus(tower, currentCell); fillTowerStatus(tower, currentCell)
                case _ =>
              }
          }
      }
    }
  }
}
