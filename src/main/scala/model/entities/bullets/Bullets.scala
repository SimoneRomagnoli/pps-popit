package model.entities.bullets

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, MovementAbility }
import model.entities.balloons.Balloons.Balloon
import model.entities.towers.Towers.Tower
import utils.Constants
import utils.Constants.Entities.Bullets._
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Bullets {

  /**
   * A [[Bullet]] is an [[Entity]] with [[MovementAbility]] that it's created by the [[Tower]]
   */
  trait Bullet extends Entity with MovementAbility {
    type Boundary = (Double, Double)
    def damage: Double

    def toString: String

    override def in(position: Vector2D): Bullet
    override def at(speed: Vector2D): Bullet
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
  abstract class BasicBullet(
      var damage: Double = bulletDefaultDamage,
      var position: Vector2D = defaultPosition,
      var speed: Vector2D = bulletDefaultSpeed,
      var boundary: (Double, Double) = bulletDefaultBoundary)
      extends Bullet {

    override def in(pos: Vector2D): Bullet = {
      position = pos
      this
    }

    override def at(velocity: Vector2D): Bullet = {
      this.speed = velocity
      this
    }

    def toString: String
  }

  /** An [[Explosion]] is a decorator for the bullet */
  trait Explosion extends Bullet {

    /** Represents the area of the explosion */
    def radius: Double

    def expand(rad: Double): Explosion = this match {
      case CannonBall(_)    => CannonBall(rad)
      case IceBall(_, time) => IceBall(rad, time)
    }
  }

  trait Fire extends Explosion

  trait Ice extends Explosion {
    def freezingTime: Double
    def freeze(time: Double): Explosion
  }

  case class Dart() extends BasicBullet {
    override def toString: String = "DART"
  }

  case class CannonBall(var radius: Double = bulletDefaultRadius) extends BasicBullet with Fire {

    override def toString: String = "CANNON-BALL"

    override def expand(rad: Double): Explosion = {
      radius = rad
      this
    }
  }

  case class IceBall(
      var radius: Double = bulletDefaultRadius,
      var freezingTime: Double = bulletFreezingTime)
      extends BasicBullet
      with Ice {

    override def toString: String = "ICE-BALL"

    override def freeze(time: Double): Explosion = {
      freezingTime = time
      this
    }

  }

  implicit class RichExplosion(explosion: Explosion) {

    def include(balloon: Balloon): Boolean =
      explosion.position.intersectsWith(balloon)(explosion.radius)
  }

  implicit class RichBullet(bullet: Bullet) {

    def hit(balloon: Balloon): Boolean =
      balloon.position.x < bullet.position.x + bullet.boundary._1 &&
        balloon.position.x + balloon.boundary._1 > bullet.position.x &&
        balloon.position.y < bullet.position.y + bullet.boundary._2 &&
        balloon.position.y + balloon.boundary._2 > bullet.position.y

    def exitedFromScreen(): Boolean =
      bullet.position.x > Constants.Screen.width || bullet.position.x < 0 || bullet.position.y > Constants.Screen.height || bullet.position.y < 0
  }

  def shoot: Bullet => Bullet = {
    case _: Dart       => Dart()
    case _: IceBall    => IceBall()
    case _: CannonBall => CannonBall()
  }

}
