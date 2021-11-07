package model.entities.towers

import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.bullets.BulletValues.bulletDefaultDamage
import model.entities.bullets.Bullets.{ Dart, IceBall }
import model.entities.towers.PowerUpValues.{ boostedRatioFactor, boostedSightFactor }
import model.entities.towers.PowerUps.{ BoostedTower, Camo, Damage, Ratio, Sight }
import model.entities.towers.TowerTypes.{ Arrow, Ice }
import model.entities.towers.TowerValues.{
  maxLevel,
  shotRatios,
  sightRanges,
  towerDefaultShotRatio,
  towerDefaultSightRange
}
import model.entities.towers.Towers.Tower
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TowerUpgradesTest extends AnyWordSpec with Matchers {

  val arrowTower: Tower[Dart] =
    (Arrow tower) sight towerDefaultSightRange ratio towerDefaultShotRatio

  val defaultLevel: Int = 1
  val nextLevel: Int = defaultLevel + 1

  "Tower PowerUps" when {
    "the ratio is boosted" should {
      "change the shooting frequency" in {

        (arrowTower boost Ratio).shotRatio shouldBe shotRatios(
          arrowTower.bullet
        ) * (1 / boostedRatioFactor)
        (arrowTower sightRange) shouldBe sightRanges(arrowTower.bullet)
      }
    }
    "the sight is boosted" should {
      "change the sight range" in {
        (arrowTower boost Sight).sightRange shouldBe sightRanges(
          arrowTower.bullet
        ) * boostedSightFactor
        (arrowTower shotRatio) shouldBe shotRatios(arrowTower.bullet)
      }
    }
    "the tower is completely boosted" should {
      "have the boosted values of sight and ratio" in {
        val boostedTower: Tower[Dart] = arrowTower boost Sight boost Ratio
        (boostedTower sightRange) shouldBe sightRanges(arrowTower.bullet) * boostedSightFactor
        (boostedTower shotRatio) shouldBe shotRatios(arrowTower.bullet) * (1 / boostedRatioFactor)
      }
    }
    "the tower gets its sight boosted" should {
      "detect a balloon that it couldn't see before" in {
        val balloon: Balloon =
          Simple(
            position = (arrowTower.sightRange + 2, arrowTower.position.y),
            boundary = (1.0, 1.0)
          )
        (arrowTower canSee balloon) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Sight
        (boostedTower canSee balloon) shouldBe true
      }
    }
    "the tower gets its damage boosted" should {
      "increment the bullet's damage value" in {
        arrowTower.bullet.damage shouldBe bulletDefaultDamage
        val boostedTower: Tower[Dart] = arrowTower boost Damage
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
        boostedTower.bullet.damage shouldBe (bulletDefaultDamage * Damage.factor)
      }
    }
    "a normal tower sees a camo balloon" should {
      "not detect it, unless it is boosted" in {
        val camo: Balloon = CamoBalloon(Simple(position = (1.0, 1.0), boundary = (1.0, 1.0)))
        (arrowTower canSee camo) shouldBe false
        val boostedTower: Tower[Dart] = arrowTower boost Camo
        (boostedTower canSee camo) shouldBe true
        boostedTower.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
  }

  "PowerUps Levels" when {
    "a tower is boosted" should {
      "increment their stats levels" in {
        val tower: Tower[IceBall] = Ice tower

        tower levelOf Ratio shouldBe defaultLevel
        tower levelOf Sight shouldBe defaultLevel
        tower levelOf Damage shouldBe defaultLevel

        var boostedTower: Tower[IceBall] = tower boost Ratio
        // (boostedTower shotRatio) shouldBe boostedRatio

        boostedTower levelOf Ratio shouldBe nextLevel
        boostedTower levelOf Sight shouldBe defaultLevel
        boostedTower levelOf Damage shouldBe defaultLevel

        boostedTower = boostedTower boost Sight
        // (boostedTower sightRange) shouldBe boostedSight

        boostedTower levelOf Ratio shouldBe nextLevel
        boostedTower levelOf Sight shouldBe nextLevel
        boostedTower levelOf Damage shouldBe defaultLevel

        boostedTower = boostedTower boost Damage
        boostedTower levelOf Ratio shouldBe nextLevel
        boostedTower levelOf Sight shouldBe nextLevel
        boostedTower levelOf Damage shouldBe nextLevel

        boostedTower = boostedTower boost Camo
        boostedTower levelOf Ratio shouldBe nextLevel
        boostedTower levelOf Sight shouldBe nextLevel
        boostedTower levelOf Damage shouldBe nextLevel
      }
    }
    "a tower is max boosted" should {
      "have reached max level" in {
        var tower: Tower[Dart] = Arrow tower

        tower = tower boost Ratio
        tower = tower boost Ratio

        tower levelOf Ratio shouldBe maxLevel
      }
    }
  }

}
