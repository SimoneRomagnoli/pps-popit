package view.controllers

import commons.CommonValues.Maps.outerCell
import commons.Futures.retrieve
import controller.Controller.ControllerMessages._
import controller.interaction.Messages
import controller.interaction.Messages._
import model.actors.TowerMessages.TowerBoosted
import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.PowerUps.{ BoostedTower, Camo, Damage, Ratio, Sight, TowerPowerUp }
import model.entities.towers.TowerTypes
import model.entities.towers.TowerTypes.TowerType
import model.entities.towers.TowerValues.maxLevel
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.BoostTowerIn
import model.maps.Cells.Cell
import model.stats.Stats.GameStats
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Cursor
import scalafx.scene.control.{ Button, Label }
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout._
import scalafx.scene.shape.Shape
import scalafxml.core.macros.sfxml
import view.render.Rendering
import view.render.Rendering.Drawers._

import scala.concurrent.Future

/**
 * Controller of the game menu. This controller loads the lateral right-menu of the game and
 * contains elements with the actual status of the game.
 */
trait ViewGameMenuController extends GameControllerChild {
  def clearForwardStatus(): Unit
  def nextRound(): Unit
  def setup(): Unit
  def setHighlightingTower(reference: Option[Tower[_]] => Unit): Unit
  def renderStats(stats: GameStats): Unit
  def anyTowerSelected(): Boolean
  def unselectDepot(): Unit
  def fillTowerStatus(tower: Tower[Bullet], cell: Cell): Unit
  def clearTowerStatus(): Unit
  def getSelectedTowerType[B <: Bullet]: TowerType[B]
  def disableRoundButton(): Unit
  def enableRoundButton(): Unit
  def disableAllButtons(): Unit
  def enableAllButtons(): Unit
}

/**
 * Controller class bound to the game menu fxml.
 */
@sfxml
class GameMenuController(
    val gameMenu: VBox,
    val gameStatus: HBox,
    val statusUpperBox: HBox,
    val lifeLabel: Label,
    val statusLowerBox: HBox,
    val moneyLabel: Label,
    val roundLabel: Label,
    val towerDepot: VBox,
    val towerStatus: VBox,
    val startRoundContainer: VBox,
    val startRound: Button,
    val pauseRound: Button,
    val forwardRounds: Button,
    var automaticRounds: Boolean,
    var currentCell: Cell = outerCell,
    var roundOver: Boolean = true,
    var parent: ViewGameController,
    var send: Input => Unit,
    var ask: Message => Future[Message],
    var highlight: Option[Tower[_]] => Unit,
    var selectedTowerType: TowerType[_])
    extends ViewGameMenuController {
  import MenuSetters._

  override def setup(): Unit = Platform runLater {
    reset()
    setLayout()
    setupButtons()
    setupTowerDepot()
  }

  override def setSend(reference: Messages.Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = gameMenu.visible = true
  override def hide(): Unit = gameMenu.visible = false
  override def setParent(controller: ViewGameController): Unit = parent = controller
  override def setLayout(): Unit = setSpacing()

  override def setTransparency(): Unit = {}

  override def reset(): Unit = resetMenu()

  override def setHighlightingTower(reference: Option[Tower[_]] => Unit): Unit =
    highlight = reference

  override def anyTowerSelected(): Boolean =
    towerDepot.children.map(_.getStyleClass.contains("selected")).reduce(_ || _)

  override def unselectDepot(): Unit =
    towerDepot.children.foreach(_.getStyleClass.remove("selected"))

  override def getSelectedTowerType[B <: Bullet]: TowerType[B] =
    selectedTowerType.asInstanceOf[TowerType[B]]

  override def renderStats(stats: GameStats): Unit = {
    lifeLabel.text = stats.life.toString
    moneyLabel.text = stats.wallet.toString + "$"
    roundLabel.text = stats.round.toString
  }

  override def fillTowerStatus(tower: Tower[Bullet], cell: Cell): Unit = Platform runLater {
    clearTowerStatus()
    if (currentCell == cell) {
      currentCell = outerCell
      highlight(None)
    } else {
      currentCell = cell
      addToTowerStatus((Rendering a tower).asSingle)
      addToTowerStatus("Sight Range", tower levelOf Sight, Sight)
      addToTowerStatus("Bullet Damage", tower levelOf Damage, Damage)
      addToTowerStatus("Shot Ratio", tower levelOf Ratio, Ratio)
      addToTowerStatus("Camo Vision", tower.isInstanceOf[EnhancedSightAbility], Camo)
      highlight(Some(tower))
    }
  }

  override def disableRoundButton(): Unit = startRound.disable = true

  override def enableRoundButton(): Unit =
    if (!roundOver) {
      roundOver = true
      startRound.disable = false
    }

  override def disableAllButtons(): Unit = {
    startRound.disable = true
    pauseRound.disable = true
    forwardRounds.disable = true
    towerDepot.disable = true
  }

  override def enableAllButtons(): Unit = {
    pauseRound.disable = false
    forwardRounds.disable = false
    towerDepot.disable = false
    if (roundOver && !automaticRounds) startRound.disable = false
  }

  override def nextRound(): Unit =
    if (!automaticRounds) enableRoundButton()
    else send(StartNextRound())

  override def clearForwardStatus(): Unit = {
    automaticRounds = false
    forwardRounds.styleClass -= "forwardOn"
  }

  override def clearTowerStatus(): Unit =
    towerStatus.children.clear()

  /** Private verbose methods for setting the controller. */
  private object MenuSetters {

    def resetMenu(): Unit = {
      roundOver = true
      roundLabel.text = "0"
      disableRoundButton()
      moneyLabel.text = "200$"
      lifeLabel.text = "100"
      towerDepot.children.removeRange(1, towerDepot.children.size)
      towerStatus.children.clear()
      clearForwardStatus()
    }

    def setSpacing(): Unit = {
      val space: Double = 8.0
      gameMenu.setSpacing(space)
      towerDepot.setSpacing(space)
      statusUpperBox.setAlignment(Pos.CenterLeft)
      statusLowerBox.setAlignment(Pos.CenterLeft)
      startRoundContainer.setAlignment(Pos.Center)
    }

    def setupButtons(): Unit = {
      pauseRound.onMouseClicked = _ => {
        send(PauseGame())
        disableAllButtons()
        parent.pauseController.show()
      }
      startRound.onMouseClicked = _ => {
        send(StartNextRound())
        roundOver = false
        disableRoundButton()
      }
      forwardRounds.onMouseClicked = _ => {
        startRound.disable = true
        roundOver = false
        if (automaticRounds) forwardRounds.styleClass -= "forwardOn"
        else forwardRounds.styleClass += "forwardOn"
        automaticRounds = !automaticRounds
      }
    }

    def setupTowerDepot[B <: Bullet](): Unit =
      TowerTypes.values.foreach { towerValue =>
        val towerType: TowerType[B] = towerValue.asInstanceOf[TowerType[B]]
        val tower: Tower[B] = towerType.tower
        val renderedTower: Shape = (Rendering a tower).asSingle
        val towerBox: HBox = new HBox(renderedTower)
        towerBox.styleClass += "towerBox"
        towerBox.setCursor(Cursor.Hand)
        towerBox.onMousePressed = _ =>
          if (!parent.pauseController.isPaused) {
            if (!towerBox.styleClass.contains("selected")) {
              unselectDepot()
              towerBox.styleClass += "selected"
              selectedTowerType = towerType
            } else {
              unselectDepot()
            }
          }
        towerBox.onMouseReleased = _ => towerBox.setCursor(Cursor.Hand)

        val towerLabel: Label = Label(towerType.toString().capitalize)
        val towerPrice: Label = Label(towerType.cost.toString + "$")
        towerBox.children += towerLabel
        towerBox.children += new Pane { hgrow = Always }
        towerBox.children += towerPrice
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
      val value: Label = argument match {
        case bool: Boolean => if (bool) Label("Yes") else Label("No")
        case _             => Label(argument.toString)
      }
      val emptyBox: HBox = new HBox()
      emptyBox.hgrow = Always
      val button: Button = new Button(powerUp.cost.toString + "$")
      button.onMouseClicked = _ =>
        retrieve(ask(BoostTowerIn(currentCell, powerUp))) {
          case TowerBoosted(tower, _) =>
            refreshTowerStatus(tower)
          case _ =>
        }
      argument match {
        case int: Int if int == maxLevel => button.disable = true
        case bool: Boolean if bool       => button.disable = true
        case _                           =>
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
