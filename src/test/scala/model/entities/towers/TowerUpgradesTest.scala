package model.entities.towers

import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.bullets.Bullets.Dart
import model.entities.towers.PowerUps.{ BoostedTower, Camo, Damage, Ratio, Sight }
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.Towers.Tower
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.Constants.Entities.Bullets.bulletDefaultDamage

import scala.language.postfixOps

class TowerUpgradesTest extends AnyWordSpec with Matchers {

  val boostedSight: Double = 2.0
  val boostedRatio: Double = 1.0
  val sightRange: Double = 1.0
  val shotRatio: Double = 2.0
  val defaultLevel: Int = 1

  "According to the expected system behavior" when {
    "the tower ratio is boosted" should {
      "change the shooting frequency" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        (arrowTower boost Ratio).shotRatio shouldBe boostedRatio
        (arrowTower sightRange) shouldBe sightRange
      }
    }
    "the tower sight is boosted" should {
      "change the sight range" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        (arrowTower boost Sight).sightRange shouldBe boostedSight
        (arrowTower shotRatio) shouldBe shotRatio
      }
    }
    "the tower is completely boosted" should {
      "have the boosted values of sight and ratio" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio
        val boostedTower: Tower[Dart] = arrowTower boost Sight boost Ratio
        (boostedTower sightRange) shouldBe boostedSight
        (boostedTower shotRatio) shouldBe boostedRatio
      }
    }
    "the tower get the ratio boost" should {
      "detect a balloon that can't see before" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        val balloon: Balloon = Simple(position = (1.5, 1.5), boundary = (1.0, 1.0))
        (arrowTower canSee balloon) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Sight
        (boostedTower canSee balloon) shouldBe true
      }
    }
    "the tower get the damage boost" should {
      "increment the bullet damage value" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        arrowTower.bullet.damage shouldBe bulletDefaultDamage
        val boostedTower: Tower[Dart] = arrowTower boost Damage
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
        boostedTower.bullet.damage shouldBe (bulletDefaultDamage * Damage.factor)
      }
    }
    "a normal tower see a camo balloon" should {
      "not detect it, unless if is boosted" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        val camo: Balloon = CamoBalloon(Simple(position = (1.0, 1.0), boundary = (1.0, 1.0)))
        (arrowTower canSee camo) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Camo
        (boostedTower canSee camo) shouldBe true
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
  }

  "Implementing levels for powerups" when {
    "a tower is boosted, it" should {
      "increment its stats levels" in {
        val arrowTower: Tower[Dart] =
          (Arrow tower) has values sight sightRange ratio shotRatio

        arrowTower levelOf Ratio shouldBe defaultLevel
        arrowTower levelOf Sight shouldBe defaultLevel
        arrowTower levelOf Damage shouldBe defaultLevel

        var boostedTower: Tower[Dart] = arrowTower boost Ratio
        (boostedTower shotRatio) shouldBe boostedRatio
        boostedTower levelOf Ratio shouldBe defaultLevel + 1
        boostedTower levelOf Sight shouldBe defaultLevel
        boostedTower levelOf Damage shouldBe defaultLevel

        boostedTower = boostedTower boost Sight
        (boostedTower sightRange) shouldBe boostedSight

        boostedTower levelOf Ratio shouldBe defaultLevel + 1
        boostedTower levelOf Sight shouldBe defaultLevel + 1
        boostedTower levelOf Damage shouldBe defaultLevel

        boostedTower = boostedTower boost Damage
        boostedTower levelOf Ratio shouldBe defaultLevel + 1
        boostedTower levelOf Sight shouldBe defaultLevel + 1
        boostedTower levelOf Damage shouldBe defaultLevel + 1

        boostedTower = boostedTower boost Camo
        boostedTower levelOf Ratio shouldBe defaultLevel + 1
        boostedTower levelOf Sight shouldBe defaultLevel + 1
        boostedTower levelOf Damage shouldBe defaultLevel + 1

      }
    }
  }

}
