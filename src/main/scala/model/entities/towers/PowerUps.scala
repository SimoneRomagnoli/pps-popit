package model.entities.towers

import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.{ BaseTower, Tower }
import utils.Constants.Entities.Bullets.bulletDefaultDamage
import utils.Constants.Entities.Towers.TowerPowerUps._
import utils.Constants.Entities.Towers.{ towerDefaultShotRatio, towerDefaultSightRange }

import scala.language.implicitConversions

object PowerUps {

  sealed trait PowerUp {
    def cost: Int
    def factor: Double
  }

  sealed class TowerPowerUp(override val cost: Int, override val factor: Double = 0.0)
      extends PowerUp

  case object Ratio extends TowerPowerUp(boostedRatioCost, boostedRatioFactor)
  case object Sight extends TowerPowerUp(boostedSightCost, boostedSightFactor)
  case object Damage extends TowerPowerUp(boostedDamageCost, boostedDamageFactor)
  case object Camo extends TowerPowerUp(boostedCamoCost)

  implicit class BoostedTower[B <: Bullet](tower: Tower[B]) {

    def boost(powerUp: TowerPowerUp): Tower[B] =
      powerUp match {
        case Ratio =>
          tower has values ratio tower.shotRatio * (1 / powerUp.factor)
        case Sight =>
          tower has values sight tower.sightRange * powerUp.factor
        case Damage =>
          tower has values damage tower.bullet.hurt(tower.bullet.damage * powerUp.factor)
        case Camo =>
          new BaseTower(tower) with EnhancedSightAbility
        case _ => tower
      }

    def levelOf: PowerUp => Int = {
      case powerUp @ Ratio =>
        -log(powerUp.factor, tower.shotRatio / towerDefaultShotRatio).toInt + 1
      case powerUp @ Sight =>
        log(powerUp.factor, tower.sightRange / towerDefaultSightRange).toInt + 1
      case powerUp @ Damage =>
        log(powerUp.factor, tower.bullet.damage / bulletDefaultDamage).toInt + 1
    }

    private def log(base: Double, number: Double): Double =
      Math.log(number) / Math.log(base)
  }
}
