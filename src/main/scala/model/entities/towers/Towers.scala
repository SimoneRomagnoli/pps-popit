package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, IceBall }
import model.entities.towers.Towers.TowerBuilders._
import model.entities.towers.Towers.Tower
import utils.Constants.Entities.Towers._
import utils.Constants.Entities.Bullets._
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see balloon object in its sight range
   */
  trait Tower extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double)

    def bullet: Bullet

    override def in(pos: Vector2D): Tower
    override def rotateTo(dir: Vector2D): Tower
    override def withSightRangeOf(radius: Double): Tower
    override def withShotRatioOf(ratio: Double): Tower
  }

  trait TowerBuilder[B <: Bullet] {
    def build(bullet: B): Tower
  }

  object TowerBuilders {
    implicit val monkeyTowerBuilder: TowerBuilder[Dart] = MonkeyTower(_)
    implicit val iceTowerBuilder: TowerBuilder[IceBall] = IceTower(_)
    implicit val cannonTowerBuilder: TowerBuilder[CannonBall] = CannonTower(_)
  }

  def instance[B <: Bullet](bullet: B)(implicit towerBuilder: TowerBuilder[B]): Tower =
    towerBuilder.build(bullet)

  class BaseTower(
      override val bullet: Bullet,
      override val boundary: (Double, Double) = towerDefaultBoundary,
      override val position: Vector2D = defaultPosition,
      override val sightRange: Double = towerDefaultSightRange,
      override val shotRatio: Double = towerDefaultShotRatio,
      override val direction: Vector2D = towerDefaultDirection)
      extends Tower {

    override def in(pos: Vector2D): Tower =
      new BaseTower(bullet, boundary, pos, sightRange, shotRatio, direction)

    override def rotateTo(dir: Vector2D): Tower =
      new BaseTower(bullet, boundary, position, sightRange, shotRatio, dir)

    override def withSightRangeOf(radius: Double): Tower =
      new BaseTower(bullet, boundary, position, radius, shotRatio, direction)

    override def withShotRatioOf(ratio: Double): Tower =
      new BaseTower(bullet, boundary, position, sightRange, ratio, direction)
  }

  case class MonkeyTower(override val bullet: Dart) extends BaseTower(bullet)
  case class IceTower(override val bullet: IceBall) extends BaseTower(bullet)
  case class CannonTower(override val bullet: CannonBall) extends BaseTower(bullet)
}

object TowerTypes {

  sealed trait Ammo {
    def bullet: Bullet
  }

  sealed class TowerAmmo(override val bullet: Bullet) extends Ammo

  case object Monkey
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

  implicit class TowerType(ammo: TowerAmmo) {

    import Towers.instance

    def tower: Tower = ammo bullet match {
      case dart: Dart             => instance(dart)
      case iceBall: IceBall       => instance(iceBall)
      case cannonBall: CannonBall => instance(cannonBall)
      case _                      => null
    }
  }
}
