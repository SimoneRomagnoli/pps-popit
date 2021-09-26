package model.entities.bullets

import model.Positions
import model.Positions.Vector2D
import model.entities.Entities.{ Entity, MovementAbility }
import utils.Constants.Entities.Bullets.{
  bulletDefaultBoundary,
  bulletDefaultDamage,
  bulletDefaultSpeed
}
import utils.Constants.Entities.defaultPosition

import scala.language.{ implicitConversions, postfixOps }

object Bullets {

  trait Bullet extends Entity with MovementAbility {
    type Boundary = (Double, Double)
    def damage: Double
  }

  abstract class BasicBullet(
      override val damage: Double = bulletDefaultDamage,
      override val position: Vector2D = defaultPosition,
      override val speed: Vector2D = bulletDefaultSpeed,
      override val boundary: (Double, Double) = bulletDefaultBoundary)
      extends Bullet {

    override def in(position: Positions.Vector2D): Entity = this match {
      case Dart(damage, _, speed, boundary) => Dart(damage, position, speed, boundary)
      case CannonBall(damage, _, speed, radius, boundary) =>
        CannonBall(damage, position, speed, radius, boundary)
      case IceBall(damage, _, speed, time, radius, boundary) =>
        IceBall(damage, position, speed, time, radius, boundary)
    }

    override def at(speed: Positions.Vector2D): Entity = this match {
      case Dart(damage, position, _, boundary) => Dart(damage, position, speed, boundary)
      case CannonBall(damage, position, _, radius, boundary) =>
        CannonBall(damage, position, speed, radius, boundary)
      case IceBall(damage, position, _, time, radius, boundary) =>
        IceBall(damage, position, speed, time, radius, boundary)
    }
  }

  trait Explosion {
    def radius: Double

    def expand(rad: Double): Explosion = this match {
      case CannonBall(damage, position, speed, _, boundary) =>
        CannonBall(damage, position, speed, rad, boundary)
      case IceBall(damage, position, speed, _, time, boundary) =>
        IceBall(damage, position, speed, time, rad, boundary)
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
      extends BasicBullet(damage, position, speed, boundary)

  case class CannonBall(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D,
      override val radius: Double,
      override val boundary: (Double, Double))
      extends BasicBullet(damage, position, speed)
      with Fire

  case class IceBall(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D,
      override val radius: Double,
      override val freezingTime: Double,
      override val boundary: (Double, Double))
      extends BasicBullet(damage, position, speed)
      with Ice {

    override def freeze(time: Double): Explosion =
      IceBall(damage, position, speed, radius, time, boundary)
  }
}
