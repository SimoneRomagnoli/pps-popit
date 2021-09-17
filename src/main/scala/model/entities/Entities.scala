package model.entities

import model.Positions.Vector2D
import model.Positions._

object Entities {

  /**
   * Basic entity of the system which:
   *   - has a position
   *   - can update its position
   *   - can update itself
   */
  trait Entity {
    def position: Vector2D
    def in(position: Vector2D): Entity
    def update(dt: Double): Entity = this
  }

  /**
   * Adds to the [[Entity]] the ability to move:
   *   - has a speed
   *   - can update its speed
   */
  trait MovementAbility extends Entity {
    def speed: Vector2D
    def at(speed: Vector2D): Entity
    private def move(dt: Double): Entity = this in (position + (speed * dt))

    abstract override def update(dt: Double): Entity =
      super.update(dt).asInstanceOf[MovementAbility] move dt
  }

  /**
   * Adds to the [[Entity]] the ability to be popped.
   */
  trait Poppable extends Entity {
    def life: Int
    def pop(bullet: Entity): Option[Entity]
  }

}
