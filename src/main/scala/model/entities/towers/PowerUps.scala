package model.entities.towers

import model.entities.Entities.EnhancedSightAbility
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.{ BaseTower, Tower }
import utils.Constants.Entities.Towers.TowerPowerUps._

import scala.collection.mutable
import scala.language.implicitConversions

object PowerUps {

  sealed trait PowerUp {
    def cost: Int
    def factor: Double
  }

  sealed class TowerPowerUp(override val cost: Int, override val factor: Double = 0.0)
      extends PowerUp

  case object Ratio extends TowerPowerUp(boostedRatioCost, boostedRatioFactor) {
    override def toString: String = "ratio"
  }

  case object Sight extends TowerPowerUp(boostedSightCost, boostedSightFactor) {
    override def toString: String = "sight"
  }

  case object Damage extends TowerPowerUp(boostedDamageCost, boostedDamageFactor) {
    override def toString: String = "damage"
  }
  case object Camo extends TowerPowerUp(boostedCamoCost)

  implicit class BoostedTower[B <: Bullet](tower: Tower[B]) {

    def boost(powerUp: TowerPowerUp): Tower[B] = {
      if (powerUp.toString != "Camo") tower levelUp powerUp.toString
      val levels: mutable.Map[String, Int] = tower.statsLevels
      powerUp match {
        case Ratio =>
          tower has values ratio tower.shotRatio / powerUp.factor stats levels
        case Sight =>
          tower has values sight tower.sightRange * powerUp.factor stats levels
        case Damage =>
          tower has values damage tower.bullet.hurt(
            tower.bullet.damage * powerUp.factor
          ) stats levels
        case Camo =>
          new BaseTower(tower) with EnhancedSightAbility
        case _ => tower
      }
    }
  }
}
