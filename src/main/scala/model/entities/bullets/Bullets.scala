package model.entities.bullets

import model.Positions
import model.Positions.Vector2D
import model.entities.Entities.{ Entity, MovementAbility }
import model.entities.balloons.Balloons.Balloon
import utils.Constants.Entities.Bullets.{
  bulletDefaultBoundary,
  bulletDefaultDamage,
  bulletDefaultRadius,
  bulletDefaultSpeed,
  bulletFreezingTime
}
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Bullets {

  trait Bullet extends Entity with MovementAbility {
    type Boundary = (Double, Double)
    def damage: Double
    def hit(balloon: Balloon): Boolean
  }

  abstract class BasicBullet(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary)
      extends Bullet {

    override def in(pos: Vector2D): Entity = instance(damage, pos, speed, boundary)

    override def at(velocity: Vector2D): Entity = instance(damage, position, velocity, boundary)

    def instance(
        damage: Double,
        position: Vector2D,
        speed: Vector2D,
        boundary: (Double, Double)): Bullet

    override def hit(balloon: Balloon): Boolean =
      balloon.position.x < position.x + boundary._1 && balloon.position.x + balloon.boundary._1 > position.x && balloon.position.y < position.y + boundary._2 && balloon.position.y + balloon.boundary._2 > position.y
  }

  trait Explosion {
    def radius: Double

    def expand(rad: Double): Explosion = this match {
      case CannonBall(damage, position, speed, boundary, _) =>
        CannonBall(damage, position, speed, boundary, rad)
      case IceBall(damage, position, speed, boundary, _, time) =>
        IceBall(damage, position, speed, boundary, rad, time)
    }
  }

  trait Fire extends Explosion

  trait Ice extends Explosion {
    def freezingTime: Double
    def freeze(time: Double): Explosion
  }

  case class Dart(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary)
      extends BasicBullet(damage, position, speed, boundary) {

    override def instance(
        damage: Double,
        position: Vector2D,
        speed: Vector2D,
        boundary: (Double, Double)): Bullet = Dart(damage, position, speed, boundary)

  }

  case class CannonBall(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary,
      override val radius: Double = bulletDefaultRadius)
      extends BasicBullet(damage, position, speed)
      with Fire {

    override def instance(
        damage: Double,
        position: Vector2D,
        speed: Vector2D,
        boundary: (Double, Double)): Bullet = CannonBall(damage, position, speed, boundary, radius)
  }

  case class IceBall(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary,
      override val radius: Double = bulletDefaultRadius,
      override val freezingTime: Double = bulletFreezingTime)
      extends BasicBullet(damage, position, speed)
      with Ice {

    override def instance(
        damage: Double,
        position: Vector2D,
        speed: Vector2D,
        boundary: (Double, Double)): Bullet =
      IceBall(damage, position, speed, boundary, radius, freezingTime)

    override def freeze(time: Double): Explosion =
      IceBall(damage, position, speed, boundary, radius, time)
  }
}
