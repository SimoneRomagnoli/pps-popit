package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.TowerTypes.{ Base, Cannon, Ice }
import model.entities.towers.Towers.{ BaseTower, CannonTower, IceTower, Tower }
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.Entities.Towers.{ towerDefaultShotRatio, towerDefaultSightRange }
import utils.Constants.Entities.defaultPosition

import scala.language.postfixOps

object TowerTypesTest {}

class TowerTypesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "According to the specified type of the tower" when {
    "a base tower is spawned, it" should {
      "have the default attributes" in {
        val baseTower: Tower =
          (Base tower) in defaultPosition withSightRangeOf towerDefaultSightRange withShotRatioOf towerDefaultShotRatio
        (baseTower position) shouldBe defaultPosition
        (baseTower sightRange) shouldBe towerDefaultSightRange
        (baseTower shotRatio) shouldBe towerDefaultShotRatio
        baseTower.isInstanceOf[BaseTower] shouldBe true
        baseTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    "a cannon tower is spawned, it" should {
      "have the default attributes" in {
        val cannonTower: Tower =
          (Cannon tower) in defaultPosition withSightRangeOf towerDefaultSightRange withShotRatioOf towerDefaultShotRatio
        (cannonTower position) shouldBe defaultPosition
        (cannonTower sightRange) shouldBe towerDefaultSightRange
        (cannonTower shotRatio) shouldBe towerDefaultShotRatio
        cannonTower.isInstanceOf[CannonTower] shouldBe true
        cannonTower.bullet.isInstanceOf[CannonBall] shouldBe true
      }
    }
    "an ice tower is spawned, it" should {
      "have the default attributes" in {
        val iceTower: Tower =
          (Ice tower) in defaultPosition withSightRangeOf towerDefaultSightRange withShotRatioOf towerDefaultShotRatio
        (iceTower position) shouldBe defaultPosition
        (iceTower sightRange) shouldBe towerDefaultSightRange
        (iceTower shotRatio) shouldBe towerDefaultShotRatio
        iceTower.isInstanceOf[IceTower] shouldBe true
        iceTower.bullet.isInstanceOf[IceBall] shouldBe true
      }
    }
  }
}
