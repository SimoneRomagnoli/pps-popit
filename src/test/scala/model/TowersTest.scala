package model

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.TowersTest.{balloon, balloonPosition, towerPosition}
import model.tower.Position.Position
import model.tower.Tower.{SimpleTower, Tower}
import org.scalatest.wordspec.AnyWordSpecLike

object TowersTest {

  val balloonPosition: Position = (6.0, 8.0)
  val towerPosition: Position = (2.0, 1.0)

  case class Balloon(var radius: Double, var position: Position) {
    def moveTo(to: Position): Balloon = {
      position = to
      this
    }
  }

  val balloon: Balloon = Balloon(1.0, balloonPosition)

}

class TowersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val tower: Tower = Tower(towerPosition)

  "At the beginning" when {
    "tower and balloon has just spawned" should {
      "be across the map and not collide" in {
        tower.position shouldBe towerPosition
        balloon.position shouldBe balloonPosition
        tower intersects(balloon.position, balloon.radius) shouldBe false
      }
    }
    "balloon goes near the tower" should {
      "be in tower sight range and collide" in {
        balloon moveTo((3.0, 2.0))
        tower.position shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower intersects(balloon.position, balloon.radius) shouldBe true
      }
    }
  }

}
