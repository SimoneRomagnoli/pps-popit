package model.entities.bullets

import model.Positions.{ defaultPosition, Vector2D }
import model.entities.Entities.{ Entity, MovementAbility, SightAbility }
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues._
import model.entities.towers.Towers.Tower
import commons.CommonValues
import commons.CommonValues.Screen.cellSize

import scala.language.{ implicitConversions, postfixOps }

object Bullets {

  /**
   * A [[Bullet]] is an [[Entity]] with [[MovementAbility]] that it's created by the [[Tower]]
   */
  trait Bullet extends Entity with MovementAbility {
    type Boundary = (Double, Double)

    override def position: Vector2D = bullet.position
    override def speed: Vector2D = bullet.speed
    override def boundary: Boundary = bullet.boundary

    override def in(pos: Vector2D): Bullet = instance(
      BasicBullet(this.damage, pos, this.speed, this.boundary)
    )

    override def at(velocity: Vector2D): Bullet = instance(
      BasicBullet(this.damage, this.position, velocity, this.boundary)
    )

    def damage: Double = bullet.damage
    def hurt(d: Double): Bullet = instance(BasicBullet(d, this.position, this.speed, this.boundary))

    def bullet: Bullet
    def instance(bullet: Bullet): Bullet

    def toString: String
  }

  /**
   * A [[BasicBullet]] is a default bullet instance
   * @param damage
   *   is the damage that inflicts to a [[Balloon]]
   * @param position
   *   is the [[Vector2D]] where the bullet is instanced
   * @param speed
   *   is the direction and speed of the bullet
   * @param boundary
   *   is the boundary of the rendered object in the grid
   */
  case class BasicBullet(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary)
      extends Bullet {
    override def instance(b: Bullet): Bullet = b

    override def bullet: Bullet = this
  }

  /** An [[Explosion]] is a bullet that applies its damage to all the entities in its sight range */
  trait Explosion extends Bullet with SightAbility

  trait Fire extends Explosion

  trait Ice extends Explosion {
    def freezingTime: Double
  }

  /** Simply wraps a [[Bullet]] */
  case class Dart(override val bullet: Bullet = BasicBullet()) extends Bullet {
    override def instance(b: Bullet): Bullet = Dart(b)

    override def toString: String = "DART"
  }

  /** Adds to the [[Bullet]] the exploding ability */
  case class CannonBall(
      override val bullet: Bullet = BasicBullet(),
      override val sightRange: Double = bulletDefaultSightRange)
      extends Bullet
      with Fire {
    override def instance(b: Bullet): Bullet = CannonBall(b, sightRange)

    override def sight(radius: Double): Explosion = CannonBall(bullet, radius)

    override def toString: String = "CANNON-BALL"

  }

  /** Adds to the [[Bullet]] the exploding and freezing abilities */
  case class IceBall(
      override val bullet: Bullet = BasicBullet(),
      override val sightRange: Double = bulletDefaultSightRange,
      freezingTime: Double = bulletFreezingTime)
      extends Bullet
      with Ice {
    override def instance(b: Bullet): Bullet = IceBall(b, sightRange, freezingTime)

    override def sight(radius: Double): Explosion = IceBall(bullet, radius, freezingTime)

    override def toString: String = "ICE-BALL"

  }

  /** Pimps a [[Bullet]] with some useful operators */
  implicit class RichBullet(bullet: Bullet) {

    def hit(balloon: Balloon): Boolean =
      balloon.position.x < bullet.position.x + bullet.boundary._1 &&
        balloon.position.x + balloon.boundary._1 > bullet.position.x &&
        balloon.position.y < bullet.position.y + bullet.boundary._2 &&
        balloon.position.y + balloon.boundary._2 > bullet.position.y

    def exitedFromScreen(): Boolean =
      bullet.position.x > CommonValues.Screen.width || bullet.position.x < 0 || bullet.position.y > CommonValues.Screen.height || bullet.position.y < 0
  }

  /** Simple object for spawning a [[Bullet]] from a [[Tower]] */
  object Shooting {

    def from(tower: Tower[Bullet]): Bullet = {
      tower.bullet match {
        case _: IceBall    => IceBall()
        case _: CannonBall => CannonBall()
        case _             => Dart()
      }
    } hurt tower.bullet.damage in tower.position at tower.direction * bulletSpeedFactor
  }
}

object BulletValues {
  val bulletDefaultBoundary: (Double, Double) = (cellSize / 4, cellSize / 4)
  val bulletDefaultDamage: Double = 1.0
  val bulletDefaultSightRange: Double = cellSize / 4
  val bulletFreezingTime: Double = 1.0
  val bulletDefaultSpeed: Vector2D = (100.0, -100.0)
  val bulletSpeedFactor: Double = 1000.0
}
