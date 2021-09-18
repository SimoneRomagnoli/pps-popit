package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, SightAbility }

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  trait Tower extends Entity with SightAbility {
    type Boundary = Double

    override def withSightRangeOf(radius: Double): Tower
  }

  case class SimpleTower(
      override val position: Vector2D = (0.0, 0.0),
      override val sightRange: Double = 1.0,
      override val boundary: Double = 1.0)
      extends Tower {
    override def in(pos: Vector2D): Tower = SimpleTower(pos, sightRange)

    override def withSightRangeOf(radius: Double): Tower = SimpleTower(position, radius)
  }

  object Tower {

    def apply(position: Vector2D = (0.0, 0.0), range: Double = 1.0): Tower =
      SimpleTower() in position withSightRangeOf range

    implicit def fromPositionAndSight(tuple: (Vector2D, Double)): Tower =
      Tower(tuple._1, tuple._2)
  }

}
