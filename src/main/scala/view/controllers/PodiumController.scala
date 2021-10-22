package view.controllers

import controller.Controller.ControllerMessages.{ BoostTowerIn, NewGame }
import controller.Messages
import controller.Messages.{ Input, Message }
import model.actors.TowerMessages.TowerBoosted
import model.entities.towers.PowerUps.{ Sight, TowerPowerUp }
import scalafx.geometry.Pos
import scalafx.scene.control.{ Label, ToggleButton }
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout.{ BorderPane, HBox, VBox }
import scalafxml.core.macros.sfxml
import utils.Constants
import view.render.Drawings.{ Drawing, MenuDrawings, Title }
import view.render.Rendering

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait ViewPodiumController extends ViewController {}

@sfxml
class PodiumController(
    val podiumPane: BorderPane,
    val titleLogo: HBox,
    var send: Input => Unit,
    var ask: Message => Future[Message])
    extends ViewPodiumController {
  import PodiumSetters._
  val drawing: Drawing = Drawing(MenuDrawings())
  setup()

  override def setSend(reference: Input => Unit): Unit = send = reference
  override def setAsk(reference: Message => Future[Message]): Unit = ask = reference
  override def show(): Unit = podiumPane.visible = true
  override def hide(): Unit = podiumPane.visible = false

  private object PodiumSetters {

    def setup(): Unit = {
      Rendering.setLayout(podiumPane, Constants.Screen.width, Constants.Screen.height)
      val logo = drawing the Title
      Rendering a logo into titleLogo.children

      addGameResults("points")
      setupButtons()
    }

    def setupButtons(): Unit = {}

    def addGameResults[T](title: String): Unit = {}
  }
}
