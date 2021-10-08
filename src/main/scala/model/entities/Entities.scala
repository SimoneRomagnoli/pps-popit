package model.entities

import model.Positions.Vector2D
import model.Positions._
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.bullets.Bullets.Bullet
import model.maps.Tracks.Directions.{ DOWN, UP }
import model.maps.Tracks.Track

import scala.language.postfixOps

object Entities {

  /**
   * Basic entity of the system which:
   *   - has a boundary
   *   - has a position
   *   - can update its position
   *   - can update itself
   */
  trait Entity {

    type Boundary <: {
      def _1: Double
      def _2: Double
    }
    def boundary: Boundary
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
    protected def move(dt: Double): Entity = this in (position + (speed * dt))

    abstract override def update(dt: Double): Entity =
      super.update(dt).asInstanceOf[MovementAbility] move dt
  }

  trait TrackFollowing extends MovementAbility {
    private[this] var linearPosition: Double = 0.0

    def track: Track
    def on(track: Track): TrackFollowing

    private def following(lp: Double): TrackFollowing = {
      this.linearPosition = lp
      this
    }

    override protected def move(dt: Double): Entity = {
      linearPosition += (track directionIn linearPosition match {
        case UP | DOWN => speed.y
        case _         => speed.x
      }) * dt
      this
        .in(track exactPositionFrom linearPosition)
        .asInstanceOf[TrackFollowing]
        .following(linearPosition)
    }
  }

  /**
   * Adds to the [[Entity]] the ability to be popped.
   */
  trait Poppable extends Entity {
    def life: Int
    def pop(bullet: Entity): Option[Entity]
  }

  /**
   * Adds to the [[Entity]] the ability to see other entities within his sight range.
   */
  trait SightAbility extends Entity {
    def sightRange: Double
    def direction: Vector2D

    def rotateTo(dir: Vector2D): SightAbility
    def sight(radius: Double): SightAbility
    def isInSightOfRangeOf(balloon: Balloon): Boolean = position.intersectsWith(balloon)(sightRange)

    def canSee(balloon: Balloon): Boolean = balloon match {
      case CamoBalloon(_)       => false
      case BalloonDecoration(b) => canSee(b)
      case _                    => isInSightOfRangeOf(balloon)
    }
  }

  trait EnhancedSightAbility extends SightAbility {
    override def canSee(balloon: Balloon): Boolean = isInSightOfRangeOf(balloon)
  }

  trait ShotAbility extends Entity {
    def bullet: Bullet
    def shotRatio: Double

    def ratio(ratio: Double): ShotAbility

    def canAttackAfter: Double => Boolean =
      lastShotTime => (System.currentTimeMillis() - lastShotTime) / 1000.0 >= shotRatio

    def canShootAfter: Double => Boolean =
      _ >= shotRatio

  }

  implicit class RichEntity(entity: Entity) {

    def not(other: Entity): Boolean =
      entity != other || other.position != entity.position
  }

}
