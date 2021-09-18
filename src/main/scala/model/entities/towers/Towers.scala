package model.entities.towers

import model.Positions.{ distance, Vector2D }
import model.entities.Entities.Entity

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  trait Tower extends Entity {
    /*
     * TODO: add shot ratio and aim direction
     * */
    def sightRange: Double
    def withSightRangeOf(radius: Double): Tower
  }

  trait CollisionBox extends Tower {
    override def withSightRangeOf(radius: Double): CollisionBox

    def collidesWith(pos: Vector2D, radius: Double): Boolean =
      distance(position)(pos) < (radius + sightRange)
  }

  case class SimpleTower(
      override val position: Vector2D = (0.0, 0.0),
      override val sightRange: Double = 1.0)
      extends Tower
      with CollisionBox {
    override def in(pos: Vector2D): CollisionBox = SimpleTower(pos, sightRange)

    override def withSightRangeOf(radius: Double): CollisionBox = SimpleTower(position, radius)
  }

  object Tower {

    def apply(position: Vector2D = (0.0, 0.0), range: Double = 1.0): CollisionBox =
      SimpleTower() in position withSightRangeOf range

    implicit def fromPositionAndSight(tuple: (Vector2D, Double)): CollisionBox =
      Tower(tuple._1, tuple._2)
  }

}
