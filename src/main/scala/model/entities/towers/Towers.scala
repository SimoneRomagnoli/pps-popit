package model.entities.towers

import model.Positions.Vector2D
import model.entities.Entities.{ Entity, SightAbility }

import scala.language.{ implicitConversions, postfixOps }

object Towers {

  /**
   * A [[Tower]] is an [[Entity]] with the ability of see [[Balloon]] object in its sight range
   */
  trait Tower extends Entity with SightAbility {
    type Boundary = Double

    override def withSightRangeOf(radius: Double): Tower
  }

  /**
   * A [[SimpleTower]] is an instance of a [[Tower]] with a defined:
   *   - position in the map
   *   - sight range to detect near balloon
   *   - boundary to specify the collision box
   */
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
