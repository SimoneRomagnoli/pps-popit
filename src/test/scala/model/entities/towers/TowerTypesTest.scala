package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.TowerType.{ Base, Cannon, Ice }
import model.entities.towers.Towers.{ BaseTower, CannonTower, IceTower, Tower }
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.{ position, shotRatio, sightRange }

import scala.language.postfixOps

object TowerTypesTest {}

class TowerTypesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "According to the specified type of the tower" when {
    "a base tower is spawned, it" should {
      "have the default attributes" in {
        val baseTower: Tower =
          (Base tower) in position withSightRangeOf sightRange withShotRatioOf shotRatio
        (baseTower position) shouldBe position
        (baseTower sightRange) shouldBe sightRange
        (baseTower shotRatio) shouldBe shotRatio
        baseTower.isInstanceOf[BaseTower] shouldBe true
        baseTower.asInstanceOf[BaseTower].bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    "a cannon tower is spawned, it" should {
      "have the default attributes" in {
        val cannonTower: Tower =
          (Cannon tower) in position withSightRangeOf sightRange withShotRatioOf shotRatio
        (cannonTower position) shouldBe position
        (cannonTower sightRange) shouldBe sightRange
        (cannonTower shotRatio) shouldBe shotRatio
        cannonTower.isInstanceOf[CannonTower] shouldBe true
        cannonTower.asInstanceOf[CannonTower].bullet.isInstanceOf[CannonBall] shouldBe true
      }
    }
    "an ice tower is spawned, it" should {
      "have the default attributes" in {
        val iceTower: Tower =
          (Ice tower) in position withSightRangeOf sightRange withShotRatioOf shotRatio
        (iceTower position) shouldBe position
        (iceTower sightRange) shouldBe sightRange
        (iceTower shotRatio) shouldBe shotRatio
        iceTower.isInstanceOf[IceTower] shouldBe true
        iceTower.asInstanceOf[IceTower].bullet.isInstanceOf[IceBall] shouldBe true
      }
    }
  }
}
