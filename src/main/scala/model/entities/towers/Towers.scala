package model.entities.towers

import model.Positions.{ defaultPosition, Vector2D }
import model.entities.Entities.{ EnhancedSightAbility, Entity, ShotAbility, SightAbility }
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, Fire, Ice, IceBall }
import model.entities.towers.TowerValues._
import model.entities.towers.Towers.Tower
import model.entities.towers.Towers.TowerBuilders.genericTowerBuilder
import utils.Commons.Screen.cellSize

import scala.language.{ implicitConversions, postfixOps }

object values

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of detect a balloon within its sight range and
   * shoot it with a specific [[Bullet]] wrt its shot ratio
   *
   * @tparam B
   *   is the type of the bullet it can shoot
   */
  trait Tower[+B <: Bullet] extends Entity with SightAbility with ShotAbility {
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

  /**
   * Tower instance builder which spawn a specific [[BaseTower]] specified by its [[Bullet]] type
   */
  object TowerBuilders {
    implicit def genericTowerBuilder[B <: Bullet]: TowerBuilder[B] = BaseTower[B](_)
    implicit val dartTowerBuilder: TowerBuilder[Dart] = BaseTower[Dart](_)
    implicit val iceTowerBuilder: TowerBuilder[IceBall] = BaseTower[IceBall](_)
    implicit val cannonTowerBuilder: TowerBuilder[CannonBall] = BaseTower[CannonBall](_)
  }

  def of[B <: Bullet](bullet: B)(implicit towerBuilder: TowerBuilder[B]): Tower[B] =
    towerBuilder.build(bullet).sight(sightRanges(bullet)).ratio(shotRatios(bullet))

  /**
   * A [[BaseTower]] is a default tower instance
   * @param bullet
   *   is the type of the bullet it can shoot when it detects a balloon
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
   *   is a generic to specify the type of the bullet
   */
  case class BaseTower[B <: Bullet](
      override val bullet: B,
      override val boundary: (Double, Double) = towerDefaultBoundary,
      override val position: Vector2D = defaultPosition,
      override val direction: Vector2D = towerDefaultDirection,
      override val sightRange: Double = towerDefaultSightRange,
      override val shotRatio: Double = towerDefaultShotRatio)
      extends Tower[B] {

    def this(tower: Tower[B]) = this(
      tower.bullet,
      tower.boundary,
      tower.position,
      tower.direction,
      tower.sightRange,
      tower.shotRatio
    )

    override def in(pos: Vector2D): Tower[B] = enhanced(copy(position = pos))

    override def sight(radius: Double): Tower[B] = enhanced(copy(sightRange = radius))

    override def ratio(ratio: Double): Tower[B] = enhanced(copy(shotRatio = ratio))

    override def damage(ammo: Bullet): Tower[B] = enhanced(copy(bullet = ammo.asInstanceOf[B]))

    override def rotateTo(dir: Vector2D): Tower[B] = enhanced(copy(direction = dir))

    private def enhanced(tower: Tower[B]): Tower[B] = this match {
      case _: EnhancedSightAbility => new BaseTower(tower) with EnhancedSightAbility
      case _                       => tower
    }

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

  case class TowerType[B <: Bullet](tower: Tower[B], cost: Int) extends Val {

    def spawn: Tower[Bullet] = tower.bullet match {
      case Dart()        => Towers of Dart()
      case CannonBall(_) => Towers of CannonBall()
      case IceBall(_, _) => Towers of IceBall()
    }
  }

  val arrow: TowerType[Dart] = TowerType(Arrow tower, costs(Arrow.bullet))
  val cannon: TowerType[CannonBall] = TowerType(Cannon tower, costs(Cannon.bullet))
  val ice: TowerType[IceBall] = TowerType(Ice tower, costs(Ice.bullet))
}

object TowerValues {

  val costs: Bullet => Int = {
    case _: Ice  => 5000
    case _: Fire => 2000
    case _       => 200
  }

  val sightRanges: Bullet => Double = {
    case _: Ice  => cellSize
    case _: Fire => cellSize * 5 / 4
    case _       => cellSize * 3 / 2
  }

  val shotRatios: Bullet => Double = {
    case _: Ice  => 3.0
    case _: Fire => 2.0
    case _       => 0.5
  }

  val towerDefaultShotRatio: Double = 0.5
  val towerDefaultSightRange: Double = cellSize * 3 / 2
  val towerDefaultBoundary: (Double, Double) = (cellSize / 2, cellSize / 2)
  val towerDefaultDirection: Vector2D = (0.0, 0.0)
}
