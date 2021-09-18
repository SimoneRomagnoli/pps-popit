package model.entities.bullet

import model.Positions
import model.Positions.Vector2D
import model.entities.Entities.{ Entity, MovementAbility }

import scala.language.{ implicitConversions, postfixOps }

object Bullets {

  trait Bullet extends Entity with MovementAbility {
    def damage: Double
  }

  abstract class BasicBullet(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D)
      extends Bullet {

    override def in(position: Positions.Vector2D): Entity = this match {
      case Dart(damage, _, speed)                  => Dart(damage, position, speed)
      case CannonBall(damage, _, speed, radius)    => CannonBall(damage, position, speed, radius)
      case IceBall(damage, _, speed, time, radius) => IceBall(damage, position, speed, time, radius)
    }

    override def at(speed: Positions.Vector2D): Entity = this match {
      case Dart(damage, position, _)               => Dart(damage, position, speed)
      case CannonBall(damage, position, _, radius) => CannonBall(damage, position, speed, radius)
      case IceBall(damage, position, _, time, radius) =>
        IceBall(damage, position, speed, time, radius)
    }
  }

  trait Explosion {
    def radius: Double

    def expand(rad: Double): Explosion = this match {
      case CannonBall(damage, position, speed, _) => CannonBall(damage, position, speed, rad)
      case IceBall(damage, position, speed, _, time) =>
        IceBall(damage, position, speed, time, rad)
    }
  }

  trait Fire extends Explosion

  trait Ice extends Explosion {
    def freezingTime: Double
    def freeze(time: Double): Explosion
  }

  case class Dart(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D)
      extends BasicBullet(damage, position, speed)

  case class CannonBall(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D,
      override val radius: Double)
      extends BasicBullet(damage, position, speed)
      with Fire

  case class IceBall(
      override val damage: Double,
      override val position: Vector2D,
      override val speed: Vector2D,
      override val radius: Double,
      override val freezingTime: Double)
      extends BasicBullet(damage, position, speed)
      with Ice {
    override def freeze(time: Double): Explosion = IceBall(damage, position, speed, radius, time)
  }
}
