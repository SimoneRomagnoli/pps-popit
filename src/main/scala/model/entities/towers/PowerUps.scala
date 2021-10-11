package model.entities.towers

import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.{ BaseTower, Tower }
import utils.Constants.Entities.Towers.TowerPowerUps._

object PowerUps {

  sealed trait PowerUp {
    def cost: Int
    def factor: Double
  }

  sealed class TowerPowerUp(override val cost: Int, override val factor: Double) extends PowerUp

  case object Ratio extends TowerPowerUp(boostedRatioCost, boostedRatioFactor)
  case object Sight extends TowerPowerUp(boostedSightCost, boostedSightFactor)
  case object Camo extends TowerPowerUp(boostedCamoCost, 0.0)
  case object Damage extends TowerPowerUp(boostedDamageCost, boostedDamageFactor)

  implicit class BoostedTower[B <: Bullet](tower: Tower[B]) {

    def boost(powerUp: TowerPowerUp): Tower[B] =
      powerUp match {
        case Ratio =>
          tower has values ratio tower.shotRatio / powerUp.factor
        case Sight =>
          tower has values sight tower.sightRange * powerUp.factor
        case Damage =>
          tower has values damage tower.bullet.hurt(tower.bullet.damage * powerUp.factor)
        case Camo =>
          new BaseTower(
            tower.bullet,
            tower.boundary,
            tower.position,
            tower.sightRange,
            tower.shotRatio,
            tower.direction
          ) with EnhancedSightAbility
        case _ => tower
      }
  }
}
