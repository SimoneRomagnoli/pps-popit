package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.TowerType.{ Base, Cannon, Ice }
import model.entities.towers.Towers.{ BaseTower, CannonTower, IceTower, Tower }
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.{ defaultPosition, defaultShotRatio, defaultSightRange }

import scala.language.postfixOps

object TowerTypesTest {}

class TowerTypesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "According to the specified type of the tower" when {
    "a base tower is spawned, it" should {
      "have the default attributes" in {
        val baseTower: Tower =
          (Base tower) in defaultPosition withSightRangeOf defaultSightRange withShotRatioOf defaultShotRatio
        (baseTower position) shouldBe defaultPosition
        (baseTower sightRange) shouldBe defaultSightRange
        (baseTower shotRatio) shouldBe defaultShotRatio
        baseTower.isInstanceOf[BaseTower] shouldBe true
        baseTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    "a cannon tower is spawned, it" should {
      "have the default attributes" in {
        val cannonTower: Tower =
          (Cannon tower) in defaultPosition withSightRangeOf defaultSightRange withShotRatioOf defaultShotRatio
        (cannonTower position) shouldBe defaultPosition
        (cannonTower sightRange) shouldBe defaultSightRange
        (cannonTower shotRatio) shouldBe defaultShotRatio
        cannonTower.isInstanceOf[CannonTower] shouldBe true
        cannonTower.bullet.isInstanceOf[CannonBall] shouldBe true
      }
    }
    "an ice tower is spawned, it" should {
      "have the default attributes" in {
        val iceTower: Tower =
          (Ice tower) in defaultPosition withSightRangeOf defaultSightRange withShotRatioOf defaultShotRatio
        (iceTower position) shouldBe defaultPosition
        (iceTower sightRange) shouldBe defaultSightRange
        (iceTower shotRatio) shouldBe defaultShotRatio
        iceTower.isInstanceOf[IceTower] shouldBe true
        iceTower.bullet.isInstanceOf[IceBall] shouldBe true
      }
    }
  }
}
