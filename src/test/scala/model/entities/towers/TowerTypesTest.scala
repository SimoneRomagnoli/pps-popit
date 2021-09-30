package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.TowerTypes.{ Arrow, Cannon, Ice }
import model.entities.towers.Towers.Tower
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.Entities.Towers.{ towerDefaultShotRatio, towerDefaultSightRange }
import utils.Constants.Entities.defaultPosition

import scala.language.postfixOps

object TowerTypesTest {}

class TowerTypesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "According to the specified type of the tower" when {
    "a base tower is spawned, it" should {
      "have the default attributes" in {
        val arrowTower: Tower[Dart] = Arrow tower

        (arrowTower position) shouldBe defaultPosition
        (arrowTower sightRange) shouldBe towerDefaultSightRange
        (arrowTower shotRatio) shouldBe towerDefaultShotRatio

        arrowTower.isInstanceOf[Tower[Dart]] shouldBe true

        arrowTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    "a cannon tower is spawned, it" should {
      "have the default attributes" in {
        val cannonTower: Tower[CannonBall] = Cannon tower

        (cannonTower position) shouldBe defaultPosition
        (cannonTower sightRange) shouldBe towerDefaultSightRange
        (cannonTower shotRatio) shouldBe towerDefaultShotRatio

        cannonTower.isInstanceOf[Tower[CannonBall]] shouldBe true

        cannonTower.bullet.isInstanceOf[CannonBall] shouldBe true
      }
    }
    "an ice tower is spawned, it" should {
      "have the default attributes" in {
        val iceTower: Tower[IceBall] = Ice tower

        (iceTower position) shouldBe defaultPosition
        (iceTower sightRange) shouldBe towerDefaultSightRange
        (iceTower shotRatio) shouldBe towerDefaultShotRatio

        iceTower.isInstanceOf[Tower[IceBall]] shouldBe true

        iceTower.bullet.isInstanceOf[IceBall] shouldBe true
      }
    }
  }
}
