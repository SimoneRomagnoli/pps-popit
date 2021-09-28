package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.Positions.Vector2DImpl
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.TowerTypes.{ Cannon, Ice, Monkey }
import model.entities.towers.Towers.{ CannonTower, IceTower, MonkeyTower, Tower }
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.Entities.Towers.{ towerDefaultShotRatio, towerDefaultSightRange }
import utils.Constants.Entities.defaultPosition

import scala.language.postfixOps

object TowerTypesTest {}

class TowerTypesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "According to the specified type of the tower" when {
    "a base tower is spawned, it" should {
      "have the default attributes" in {
        val monkeyTower: Tower = Monkey tower

        (monkeyTower position) shouldBe defaultPosition
        (monkeyTower sightRange) shouldBe towerDefaultSightRange
        (monkeyTower shotRatio) shouldBe towerDefaultShotRatio
        monkeyTower.isInstanceOf[MonkeyTower] shouldBe true
        monkeyTower.bullet.isInstanceOf[Dart] shouldBe true
        monkeyTower.bullet.isInstanceOf[IceBall] shouldBe false
        monkeyTower.bullet.isInstanceOf[CannonBall] shouldBe false
      }
    }
    "a cannon tower is spawned, it" should {
      "have the default attributes" in {
        val cannonTower: Tower = Cannon tower

        (cannonTower position) shouldBe defaultPosition
        (cannonTower sightRange) shouldBe towerDefaultSightRange
        (cannonTower shotRatio) shouldBe towerDefaultShotRatio
        cannonTower.isInstanceOf[CannonTower] shouldBe true
        cannonTower.bullet.isInstanceOf[CannonBall] shouldBe true
      }
    }
    "an ice tower is spawned, it" should {
      "have the default attributes" in {
        val iceTower: Tower = Ice tower

        (iceTower position) shouldBe defaultPosition
        (iceTower sightRange) shouldBe towerDefaultSightRange
        (iceTower shotRatio) shouldBe towerDefaultShotRatio
        iceTower.isInstanceOf[IceTower] shouldBe true
        iceTower.bullet.isInstanceOf[IceBall] shouldBe true
      }
    }
  }
}
