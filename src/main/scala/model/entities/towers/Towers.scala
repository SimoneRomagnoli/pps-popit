package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ BasicBullet, CannonBall, Dart, IceBall }
import utils.Constants.{
  bulletBoundary,
  damage,
  freezingTime,
  position,
  radius,
  shotRatio,
  sightRange,
  speed,
  towerDefaultBoundary
}

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see [[Balloon]] object in its sight range
   */
  trait Tower extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double) // Base & Height

    override def withSightRangeOf(radius: Double): Tower

    override def withShotRatioOf(ratio: Double): Tower
  }

  /**
   * A [[BasicTower]] is an instance of a [[Tower]] with a defined:
   *   - position in the map
   *   - sight range to detect near balloon
   *   - shot ratio to shot bullets at a certain frequency
   */
  class BasicTower(
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double)
      extends Tower {
    override def boundary: (Double, Double) = towerDefaultBoundary

    override def in(pos: Vector2D): Tower = this match {
      case BaseTower(bullet, _, range, ratio)   => BaseTower(bullet, pos, range, ratio)
      case IceTower(bullet, _, range, ratio)    => IceTower(bullet, pos, range, ratio)
      case CannonTower(bullet, _, range, ratio) => CannonTower(bullet, pos, range, ratio)
      case _                                    => new BasicTower(pos, sightRange, shotRatio)
    }

    override def withSightRangeOf(radius: Double): Tower = this match {
      case BaseTower(bullet, position, _, ratio)   => BaseTower(bullet, position, radius, ratio)
      case IceTower(bullet, position, _, ratio)    => IceTower(bullet, position, radius, ratio)
      case CannonTower(bullet, position, _, ratio) => CannonTower(bullet, position, radius, ratio)
      case _                                       => new BasicTower(position, radius, shotRatio)
    }

    override def withShotRatioOf(ratio: Double): Tower = this match {
      case BaseTower(bullet, position, range, _)   => BaseTower(bullet, position, range, ratio)
      case IceTower(bullet, position, range, _)    => IceTower(bullet, position, range, ratio)
      case CannonTower(bullet, position, range, _) => CannonTower(bullet, position, range, ratio)
      case _                                       => new BasicTower(position, sightRange, ratio)
    }
  }

  case class BaseTower(
      bullet: Dart,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double)
      extends BasicTower(position, sightRange, shotRatio)

  case class IceTower(
      bullet: IceBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double)
      extends BasicTower(position, sightRange, shotRatio)

  case class CannonTower(
      bullet: CannonBall,
      override val position: Vector2D,
      override val sightRange: Double,
      override val shotRatio: Double)
      extends BasicTower(position, sightRange, shotRatio)

  object TowerType {

    sealed trait Ammo {
      def bullet: BasicBullet
    }

    sealed class TowerAmmo(override val bullet: BasicBullet) extends Ammo
    case object Base extends TowerAmmo(Dart(damage, position, speed, bulletBoundary))

    case object Ice
        extends TowerAmmo(IceBall(damage, position, speed, radius, freezingTime, bulletBoundary))

    case object Cannon
        extends TowerAmmo(CannonBall(damage, position, speed, radius, bulletBoundary))

    object Ammo {
      // def apply(bullet: BasicBullet): TowerAmmo = new TowerAmmo(bullet)
      def unapply(ammo: TowerAmmo): Option[BasicBullet] = Some(ammo.bullet)
    }

    implicit class TowerType(ammo: TowerAmmo) {

      def tower: BasicTower = ammo match {
        case Ammo(b) if b.isInstanceOf[Dart] =>
          BaseTower(b.asInstanceOf[Dart], position, sightRange, shotRatio)
        case Ammo(b) if b.isInstanceOf[IceBall] =>
          IceTower(b.asInstanceOf[IceBall], position, sightRange, shotRatio)
        case Ammo(b) if b.isInstanceOf[CannonBall] =>
          CannonTower(b.asInstanceOf[CannonBall], position, sightRange, shotRatio)
        case Ammo(b) => BaseTower(b.asInstanceOf[Dart], position, sightRange, shotRatio)
      }
    }
  }
}
