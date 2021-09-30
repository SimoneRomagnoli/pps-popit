package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ BoostAbility, Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, IceBall }
import model.entities.towers.TowerUpgrades.{ Ratio, Sight, TowerPowerUp }
import model.entities.towers.Towers.TowerBuilders.genericTowerBuilder
import model.entities.towers.Towers.Tower
import utils.Constants.Entities.Towers._
import utils.Constants.Entities.Bullets._
import utils.Constants.Entities.Towers.TowerPowerUps.{
  boostedRatioCost,
  boostedRatioFactor,
  boostedSightCost,
  boostedSightFactor
}
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of detect a balloon within its sight range and
   * shoot it with a specific [[Bullet]]
   *
   * @tparam B
   *   is the type of the [[Bullet]] it can shoot
   */
  trait Tower[B <: Bullet] extends Entity with SightAbility with ShotAbility with BoostAbility {
    type Boundary = (Double, Double)

    def bullet: B

    override def in(pos: Vector2D): Tower[B]
    override def rotateTo(dir: Vector2D): Tower[B]
    override def withSightRangeOf(radius: Double): Tower[B]
    override def withShotRatioOf(ratio: Double): Tower[B]
    override def boost(powerUp: TowerPowerUp): Tower[B]

  }

  trait TowerBuilder[B <: Bullet] {
    def build(bullet: B): Tower[B]
  }

  object TowerBuilders {
    implicit def genericTowerBuilder[B <: Bullet]: TowerBuilder[B] = BaseTower[B](_)
    implicit val dartTowerBuilder: TowerBuilder[Dart] = BaseTower[Dart](_)
    implicit val iceTowerBuilder: TowerBuilder[IceBall] = BaseTower[IceBall](_)
    implicit val cannonTowerBuilder: TowerBuilder[CannonBall] = BaseTower[CannonBall](_)
  }

  def of[B <: Bullet](bullet: B)(implicit towerBuilder: TowerBuilder[B]): Tower[B] =
    towerBuilder.build(bullet)

  /**
   * A [[BaseTower]] is a default tower instance
   * @param bullet
   *   is the type of the [[Bullet]] it can shoot when it detects a balloon
   * @param boundary
   *   is the boundary of the rendered object in the grid
   * @param position
   *   is the [[Vector2D]] where the tower is instanced
   * @param sightRange
   *   is the range of sight of the tower to detect the balloons
   * @param shotRatio
   *   is the frequency of shooting bullets
   * @param direction
   *   is the aim of the tower
   * @tparam B
   *   is a generic to specify the type of the [[Bullet]]
   */
  case class BaseTower[B <: Bullet](
      override val bullet: B,
      override val boundary: (Double, Double) = towerDefaultBoundary,
      override val position: Vector2D = defaultPosition,
      override val sightRange: Double = towerDefaultSightRange,
      override val shotRatio: Double = towerDefaultShotRatio,
      override val direction: Vector2D = towerDefaultDirection)
      extends Tower[B] {

    override def in(pos: Vector2D): Tower[B] =
      BaseTower(bullet, boundary, pos, sightRange, shotRatio, direction)

    override def rotateTo(dir: Vector2D): Tower[B] =
      BaseTower(bullet, boundary, position, sightRange, shotRatio, dir)

    override def withSightRangeOf(radius: Double): Tower[B] =
      BaseTower(bullet, boundary, position, radius, shotRatio, direction)

    override def withShotRatioOf(ratio: Double): Tower[B] =
      BaseTower(bullet, boundary, position, sightRange, ratio, direction)

    override def boost(powerUp: TowerPowerUp): Tower[B] =
      powerUp match {
        case Ratio =>
          BaseTower(bullet, boundary, position, sightRange, shotRatio * powerUp.factor, direction)
        case Sight =>
          BaseTower(bullet, boundary, position, sightRange * powerUp.factor, shotRatio, direction)
        case _ => BaseTower(bullet, boundary, position, sightRange, shotRatio, direction)
      }
  }
}

/**
 * Provides a DSL to build the towers
 */
object TowerTypes {

  sealed trait Ammo[B <: Bullet] {
    def bullet: B
    def tower: Tower[B] = Towers of bullet
  }

  sealed class TowerAmmo[B <: Bullet](override val bullet: B) extends Ammo[B]

  case object Arrow
      extends TowerAmmo(
        Dart(bulletDefaultDamage, defaultPosition, bulletDefaultSpeed, bulletDefaultBoundary)
      )

  case object Ice
      extends TowerAmmo(
        IceBall(
          bulletDefaultDamage,
          defaultPosition,
          bulletDefaultSpeed,
          bulletDefaultRadius,
          bulletFreezingTime,
          bulletDefaultBoundary
        )
      )

  case object Cannon
      extends TowerAmmo(
        CannonBall(
          bulletDefaultDamage,
          defaultPosition,
          bulletDefaultSpeed,
          bulletDefaultRadius,
          bulletDefaultBoundary
        )
      )
}

object TowerUpgrades {

  sealed trait PowerUp {
    def cost: Int
    def factor: Double
  }

  sealed class TowerPowerUp(override val cost: Int, override val factor: Double) extends PowerUp

  case object Ratio extends TowerPowerUp(boostedRatioCost, boostedRatioFactor)
  case object Sight extends TowerPowerUp(boostedSightCost, boostedSightFactor)

}
