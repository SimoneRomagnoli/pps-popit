package model.entities.towers

import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.BulletValues.bulletDefaultDamage
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.PowerUpValues._
import model.entities.towers.TowerValues.{ shotRatios, sightRanges }
import model.entities.towers.Towers.{ BaseTower, Tower }

import scala.language.implicitConversions

object PowerUps {

  sealed trait PowerUp {
    def cost: Int
    def factor: Double
  }

  /**
   * It represents a [[PowerUp]] which a [[Tower]] can obtain
   * @param cost
   *   the cost of the power up
   * @param factor
   *   the factor of which the tower value are boosted
   */
  sealed class TowerPowerUp(override val cost: Int, override val factor: Double = 0.0)
      extends PowerUp

  case object Ratio extends TowerPowerUp(boostedRatioCost, boostedRatioFactor)
  case object Sight extends TowerPowerUp(boostedSightCost, boostedSightFactor)
  case object Damage extends TowerPowerUp(boostedDamageCost, boostedDamageFactor)
  case object Camo extends TowerPowerUp(boostedCamoCost)

  /**
   * Provides an enriched functionality of the [[Tower]] which it can be boosted
   * @param tower
   *   the tower to boost
   * @tparam B
   *   the type of the bullet of the tower
   */
  implicit class BoostedTower[B <: Bullet](tower: Tower[B]) {

    def boost(powerUp: TowerPowerUp): Tower[B] =
      powerUp match {
        case Ratio =>
          tower ratio tower.shotRatio * (1 / powerUp.factor)
        case Sight =>
          tower sight tower.sightRange * powerUp.factor
        case Damage =>
          tower damage tower.bullet.hurt(tower.bullet.damage * powerUp.factor)
        case Camo =>
          new BaseTower(tower) with EnhancedSightAbility
        case _ => tower
      }

    def levelOf: PowerUp => Int = {
      case powerUp @ Ratio =>
        -log(powerUp.factor, tower.shotRatio / shotRatios(tower.bullet)).toInt + 1
      case powerUp @ Sight =>
        log(powerUp.factor, tower.sightRange / sightRanges(tower.bullet)).toInt + 1
      case powerUp @ Damage =>
        log(powerUp.factor, tower.bullet.damage / bulletDefaultDamage).toInt + 1
      case _ => 0
    }

    private def log(base: Double, number: Double): Double =
      Math.log(number) / Math.log(base)
  }
}

object PowerUpValues {
  val boostedRatioCost: Int = 1000
  val boostedRatioFactor: Double = 1.5
  val boostedSightCost: Int = 300
  val boostedSightFactor: Double = 1.3
  val boostedDamageCost: Int = 500
  val boostedDamageFactor: Double = 2.0
  val boostedCamoCost: Int = 2000
}
