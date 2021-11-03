package view.controllers

import controller.Controller.ControllerMessages.{
  BackToMenu,
  SetDifficulty,
  SetFrameRate,
  SetTimeRatio,
  UpdateSettings
}
import controller.interaction.Messages._
import controller.settings.Settings.Time.Constants._
import controller.settings.Settings.{ Easy, Hard, Normal, Settings }
import scalafx.scene.control.ToggleButton
import scalafx.scene.layout.BorderPane
import scalafxml.core.macros.sfxml
import commons.CommonValues
import controller.settings.Settings.Time.TimeSettings
import view.render.Rendering

import scala.concurrent.Future

trait ViewSettingsController extends ViewController {
  def update(): Unit
  def update(settings: Settings): Unit
}

/**
 * Controller class bound to the settings fxml.
 */
@sfxml
class SettingsController(
    val settings: BorderPane,
    val easyButton: ToggleButton,
    val normalButton: ToggleButton,
    val hardButton: ToggleButton,
    val normalSpeedButton: ToggleButton,
    val doubleSpeedButton: ToggleButton,
    val lowFrameRateButton: ToggleButton,
    val mediumFrameRateButton: ToggleButton,
    val highFrameRateButton: ToggleButton,
    val backToMenu: ToggleButton,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewSettingsController {
  import Setters._
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = settings.visible = true
  override def hide(): Unit = settings.visible = false

  override def update(): Unit = send(UpdateSettings())

  override def update(settings: Settings): Unit = {
    clearStyle()
    settings match {
      case Settings(difficulty, TimeSettings(frameRate, timeRatio)) =>
        difficulty match {
          case Easy   => easyButton.styleClass += "difficultySelected"
          case Normal => normalButton.styleClass += "difficultySelected"
          case Hard   => hardButton.styleClass += "difficultySelected"
        }
        frameRate match {
          case _ if frameRate == lowFrameRate =>
            lowFrameRateButton.styleClass += "frameRateSelected"
          case _ if frameRate == mediumFrameRate =>
            mediumFrameRateButton.styleClass += "frameRateSelected"
          case _ => highFrameRateButton.styleClass += "frameRateSelected"
        }
        timeRatio match {
          case _ if timeRatio == doubleTimeRatio =>
            doubleSpeedButton.styleClass += "timeRatioSelected"
          case _ => normalSpeedButton.styleClass += "timeRatioSelected"
        }
    }
  }

  /** Private verbose methods. */
  private object Setters {

    def setup(): Unit = {
      Rendering.setLayout(settings, CommonValues.Screen.width, CommonValues.Screen.height)
      setupButtons()
    }

    def setupButtons(): Unit = {
      easyButton.onMouseClicked = _ => send(SetDifficulty(Easy))
      normalButton.onMouseClicked = _ => send(SetDifficulty(Normal))
      hardButton.onMouseClicked = _ => send(SetDifficulty(Hard))
      backToMenu.onMouseClicked = _ => send(BackToMenu())
      normalSpeedButton.onMouseClicked = _ => send(SetTimeRatio(normalTimeRatio))
      doubleSpeedButton.onMouseClicked = _ => send(SetTimeRatio(doubleTimeRatio))
      lowFrameRateButton.onMouseClicked = _ => send(SetFrameRate(lowFrameRate))
      mediumFrameRateButton.onMouseClicked = _ => send(SetFrameRate(mediumFrameRate))
      highFrameRateButton.onMouseClicked = _ => send(SetFrameRate(highFrameRate))
    }

    def clearStyle(): Unit = {
      easyButton.styleClass -= "difficultySelected"
      normalButton.styleClass -= "difficultySelected"
      hardButton.styleClass -= "difficultySelected"
      lowFrameRateButton.styleClass -= "frameRateSelected"
      mediumFrameRateButton.styleClass -= "frameRateSelected"
      highFrameRateButton.styleClass -= "frameRateSelected"
      doubleSpeedButton.styleClass -= "timeRatioSelected"
      normalSpeedButton.styleClass -= "timeRatioSelected"
    }
  }
}
