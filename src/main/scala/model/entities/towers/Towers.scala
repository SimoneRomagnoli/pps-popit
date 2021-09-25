package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ BasicBullet, CannonBall, Dart, IceBall }
import utils.Constants.{
  bulletDefaultBoundary,
  defaultDamage,
  defaultPosition,
  defaultRadius,
  defaultShotRatio,
  defaultSightRange,
  defaultSpeed,
  freezingTime,
  towerDefaultBoundary,
  towerDefaultDirection
}

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see balloon object in its sight range
   */
  trait Tower extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double) // Base & Height

    override def rotateTo(dir: Vector2D): Tower

    override def withSightRangeOf(radius: Double): Tower

    override def withShotRatioOf(ratio: Double): Tower
  }

  /**
   * A [[BasicTower]] is an instance of a [[Tower]] with a defined:
   *   - position in the map
   *   - sight range to detect near balloon
   *   - shot ratio to shot bullets at a certain frequency
   */
  abstract class BasicTower(
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D,
      override val boundary: (Double, Double) = towerDefaultBoundary)
      extends Tower {

    override def rotateTo(dir: Vector2D): Tower = instance(position, sightRange, shotRatio, dir)

    override def in(pos: Vector2D): Tower = instance(pos, sightRange, shotRatio, direction)

    override def withSightRangeOf(radius: Double): Tower =
      instance(position, radius, shotRatio, direction)

    override def withShotRatioOf(ratio: Double): Tower =
      instance(position, sightRange, ratio, direction)

    def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): Tower
  }

  case class BaseTower(
      override val bullet: Dart,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): BaseTower =
      BaseTower(bullet, pos, range, ratio, dir)
  }

  case class IceTower(
      override val bullet: IceBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): IceTower =
      IceTower(bullet, pos, range, ratio, dir)
  }

  case class CannonTower(
      override val bullet: CannonBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double,
      override val direction: Vector2D)
      extends BasicTower(position, sightRange, shotRatio, direction) {

    override def instance(pos: Vector2D, range: Double, ratio: Double, dir: Vector2D): CannonTower =
      CannonTower(bullet, pos, range, ratio, dir)
  }

  object TowerType {

    sealed trait Ammo {
      def bullet: BasicBullet
    }

    sealed class TowerAmmo(override val bullet: BasicBullet) extends Ammo

    case object Base
        extends TowerAmmo(Dart(defaultDamage, defaultPosition, defaultSpeed, bulletDefaultBoundary))

    case object Ice
        extends TowerAmmo(
          IceBall(
            defaultDamage,
            defaultPosition,
            defaultSpeed,
            defaultRadius,
            freezingTime,
            bulletDefaultBoundary
          )
        )

    case object Cannon
        extends TowerAmmo(
          CannonBall(
            defaultDamage,
            defaultPosition,
            defaultSpeed,
            defaultRadius,
            bulletDefaultBoundary
          )
        )

    object Ammo {
      // def apply(bullet: BasicBullet): TowerAmmo = new TowerAmmo(bullet)
      def unapply(ammo: TowerAmmo): Option[BasicBullet] = Some(ammo.bullet)
    }

    implicit class TowerType(ammo: TowerAmmo) {

      def tower: BasicTower = ammo match {
        case Ammo(b) if b.isInstanceOf[Dart] =>
          BaseTower(
            b.asInstanceOf[Dart],
            defaultPosition,
            defaultSightRange,
            defaultShotRatio,
            towerDefaultDirection
          )
        case Ammo(b) if b.isInstanceOf[IceBall] =>
          IceTower(
            b.asInstanceOf[IceBall],
            defaultPosition,
            defaultSightRange,
            defaultShotRatio,
            towerDefaultDirection
          )
        case Ammo(b) if b.isInstanceOf[CannonBall] =>
          CannonTower(
            b.asInstanceOf[CannonBall],
            defaultPosition,
            defaultSightRange,
            defaultShotRatio,
            towerDefaultDirection
          )
        case Ammo(b) =>
          BaseTower(
            b.asInstanceOf[Dart],
            defaultPosition,
            defaultSightRange,
            defaultShotRatio,
            towerDefaultDirection
          )
      }
    }
  }
}
