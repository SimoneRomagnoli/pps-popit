package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, IceBall }
import model.entities.towers.Towers.Tower
import model.entities.towers.Towers.TowerBuilders.genericTowerBuilder
import utils.Constants.Entities.Towers.TowerTypes.towerDefaultCost
import utils.Constants.Entities.Towers._
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object values

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of detect a balloon within its sight range and
   * shoot it with a specific [[Bullet]]
   *
   * @tparam B
   *   is the type of the [[Bullet]] it can shoot
   */
  trait Tower[B <: Bullet] extends Entity with SightAbility with ShotAbility {
    type Boundary = (Double, Double)

    def bullet: B

    override def in(pos: Vector2D): Tower[B]
    override def rotateTo(dir: Vector2D): Tower[B]

    def has(v: values.type): Tower[B] = this

    override def ratio(ratio: Double): Tower[B]

    override def sight(radius: Double): Tower[B]

    override def damage(ammo: Bullet): Tower[B]

    override def toString: String = "towers/" + bullet.toString + "-TOWER"

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

    def this(tower: Tower[B]) = this(
      tower.bullet,
      tower.boundary,
      tower.position,
      tower.sightRange,
      tower.shotRatio,
      tower.direction
    )

    override def in(pos: Vector2D): Tower[B] = copy(position = pos)

    override def sight(radius: Double): Tower[B] = copy(sightRange = radius)

    override def ratio(ratio: Double): Tower[B] = copy(shotRatio = ratio)

    override def damage(ammo: Bullet): Tower[B] = copy(bullet = ammo.asInstanceOf[B])

    override def rotateTo(dir: Vector2D): Tower[B] = copy(direction = dir)

  }

}

/**
 * Provides a DSL to build a [[Tower]]
 */
object TowerTypes extends Enumeration {

  sealed trait Ammo[B <: Bullet] {
    def bullet: B
    def tower: Tower[B] = Towers of bullet
  }

  sealed class TowerAmmo[B <: Bullet](override val bullet: B) extends Ammo[B]

  case object Arrow extends TowerAmmo(Dart())
  case object Ice extends TowerAmmo(IceBall())
  case object Cannon extends TowerAmmo(CannonBall())

  case class TowerType[B <: Bullet](tower: Tower[B], cost: Int) extends Val
  val arrow: TowerType[Dart] = TowerType(Arrow tower, towerDefaultCost)
  val cannon: TowerType[CannonBall] = TowerType(Cannon tower, towerDefaultCost)
  val ice: TowerType[IceBall] = TowerType(Ice tower, towerDefaultCost)
}
