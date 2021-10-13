package view.controllers

import controller.Controller.ControllerMessages.{
  BoostTowerIn,
  PauseGame,
  ResumeGame,
  StartNextRound
}
import controller.Messages
import controller.Messages._
import model.actors.TowerMessages.TowerBoosted
import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.PowerUps.{ Camo, Damage, Ratio, Sight, TowerPowerUp }
import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.Towers.Tower
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
  def updateStats(stats: GameStats): Unit
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
    val statusMiddleBox: HBox,
    val pointsLabel: Label,
    val statusLowerBox: HBox,
    val moneyLabel: Label,
    val towerDepot: VBox,
    val towerStatus: VBox,
    val startRoundContainer: VBox,
    val startRound: ToggleButton,
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
  override def show(): Unit = gameMenu.visible = true
  override def hide(): Unit = gameMenu.visible = false

  override def setHighlightingTower(reference: (Tower[_], Boolean) => Unit): Unit = highlight =
    reference

  override def isPaused: Boolean = paused

  override def anyTowerSelected(): Boolean =
    towerDepot.children.map(_.getStyleClass.contains("selected")).reduce(_ || _)

  override def unselectDepot(): Unit =
    towerDepot.children.foreach(_.getStyleClass.remove("selected"))

  override def getSelectedTowerType[B <: Bullet]: TowerType[B] =
    selectedTowerType.asInstanceOf[TowerType[B]]

  override def updateStats(stats: GameStats): Unit = {
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
      addToTowerStatus("Damage", tower.bullet.damage, Damage)
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
      val space: Double = 8.0
      gameMenu.setSpacing(space)
      towerDepot.setSpacing(space)
      statusUpperBox.setAlignment(Pos.CenterLeft)
      statusLowerBox.setAlignment(Pos.CenterLeft)
      startRoundContainer.setAlignment(Pos.Center)
    }

    def setupButtons(): Unit = {
      //exitButton.onMouseClicked = _ =>
      playButton.onMouseClicked = _ =>
        if (paused) {
          send(ResumeGame())
          paused = false
        } else {
          send(PauseGame())
          paused = true
        }

      startRound.onMouseClicked = _ => {
        send(StartNextRound())
        startRound.disable = true
      }
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

    def refreshTowerStatus(tower: Tower[Bullet]): Unit = {
      fillTowerStatus(tower, currentCell)
      fillTowerStatus(tower, currentCell)
    }

    def addToTowerStatus[T](title: String, argument: T, powerUp: TowerPowerUp): Unit = {
      val box: HBox = new HBox()
      val key: Label = Label(title + ": ")
      val value: Label = Label(argument.toString)
      val emptyBox: HBox = new HBox()
      emptyBox.hgrow = Always
      val button: ToggleButton = new ToggleButton(powerUp.cost.toString)
      button.onMouseClicked = _ =>
        ask(BoostTowerIn(currentCell, powerUp)) onComplete {
          case Success(value) =>
            value match {
              case TowerBoosted(tower, _) =>
                refreshTowerStatus(tower)
            }
          case Failure(exception) => println(exception)
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
  }
}
