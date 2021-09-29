package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.entities.bullets.Bullets.Dart
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.TowerUpgrades.{ Ratio, Sight }
import model.entities.towers.TowerUpgradesTest.{ boostedTime, isStillBoosted, waitSomeTime }
import model.entities.towers.Towers.Tower
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

object TowerUpgradesTest {

  var boostedTime: Double = 0.0

  def isStillBoosted: (Double, Double) => Boolean = (time, powerUp) =>
    (System.currentTimeMillis() - time) / 1000.0 <= powerUp

  def waitSomeTime(): Unit = Thread.sleep(1000)

}

class TowerUpgradesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  val boostedSight: Double = 2.0
  val boostedRatio: Double = 4.0
  val sightRange: Double = 1.0
  val shotRatio: Double = 2.0

  "According to the expected system behavior" when {
    "the tower ratio is boosted" should {
      "change the shooting frequency" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio
        (arrowTower boost Ratio).shotRatio shouldBe boostedRatio
        (arrowTower sightRange) shouldBe sightRange
      }
    }
    "the tower sight is boosted" should {
      "change the sight range" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio
        (arrowTower boost Sight).sightRange shouldBe boostedSight
        (arrowTower shotRatio) shouldBe shotRatio
      }
    }
    "the tower is completely boosted" should {
      "have the boosted values of sight and ratio" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio
        val boostedTower: Tower[Dart] = arrowTower boost Sight boost Ratio
        (boostedTower sightRange) shouldBe boostedSight
        (boostedTower shotRatio) shouldBe boostedRatio
      }
    }
    "the tower get the ratio power up" should {
      "return at the beginning state after 3 seconds" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) withSightRangeOf sightRange withShotRatioOf shotRatio

        (arrowTower boost Ratio).shotRatio shouldBe boostedRatio
        boostedTime = System.currentTimeMillis().toDouble

        isStillBoosted(boostedTime, Ratio.time) shouldBe true
        waitSomeTime()
        isStillBoosted(boostedTime, Ratio.time) shouldBe true
        waitSomeTime()
        isStillBoosted(boostedTime, Ratio.time) shouldBe true
        waitSomeTime()
        isStillBoosted(boostedTime, Ratio.time) shouldBe false
      }
    }
  }

}
