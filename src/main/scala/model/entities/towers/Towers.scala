package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, IceBall }
import model.entities.towers.Towers.TowerBuilders.genericTowerBuilder
import model.entities.towers.Towers.Tower
import utils.Constants.Entities.Towers._
import utils.Constants.Entities.Bullets._
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see balloon object in its sight range
   */
  trait Tower[B <: Bullet] extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double)

    def bullet: B

    override def in(pos: Vector2D): Tower[B]
    override def rotateTo(dir: Vector2D): Tower[B]
    override def withSightRangeOf(radius: Double): Tower[B]
    override def withShotRatioOf(ratio: Double): Tower[B]

    override def toString: String = "towers/" + bullet.toString + "-TOWER"
  }

  trait TowerBuilder[B <: Bullet] {
    def build(bullet: B): Tower[B]
  }

  object TowerBuilders {
    implicit def genericTowerBuilder[B <: Bullet]: TowerBuilder[B] = BaseTower[B](_)
    implicit val monkeyTowerBuilder: TowerBuilder[Dart] = BaseTower[Dart](_)
    implicit val iceTowerBuilder: TowerBuilder[IceBall] = BaseTower[IceBall](_)
    implicit val cannonTowerBuilder: TowerBuilder[CannonBall] = BaseTower[CannonBall](_)
  }

  def of[B <: Bullet](bullet: B)(implicit towerBuilder: TowerBuilder[B]): Tower[B] =
    towerBuilder.build(bullet)

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
  }
}

object TowerTypes extends Enumeration {

  sealed trait Ammo[B <: Bullet] {
    def bullet: B
    def tower: Tower[B] = Towers of bullet
  }

  sealed class TowerAmmo[B <: Bullet](override val bullet: B) extends Ammo[B]

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

  case class TowerType[B <: Bullet](tower: Tower[B]) extends Val
  val monkey: TowerType[Dart] = TowerType(Monkey tower)
  val cannon: TowerType[CannonBall] = TowerType(Cannon tower)
  val ice: TowerType[IceBall] = TowerType(Ice tower)
}
