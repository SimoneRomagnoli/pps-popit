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
   *   - can change its position
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
   *   - can change its speed
   */
  trait MovementAbility extends Entity {
    def speed: Vector2D
    def at(speed: Vector2D): Entity
    protected def move(dt: Double): Entity = this in (position + (speed * dt))

    abstract override def update(dt: Double): Entity =
      super.update(dt).asInstanceOf[MovementAbility] move dt
  }

  /**
   * Adds to the [[Entity]] the ability to follow a [[Track]]:
   *   - has a [[Track]]
   *   - can change the [[Track]]
   */
  trait TrackFollowing extends MovementAbility with Comparable[TrackFollowing] { balloon: Balloon =>
    private var linearPosition: Double = 0.0

    def track: Track
    def on(track: Track): TrackFollowing

    def following(trackFollowing: TrackFollowing): Balloon =
      following(trackFollowing.linearPosition).asInstanceOf[Balloon]

    private def following(lp: Double): TrackFollowing = {
      this.linearPosition = lp
      this
    }

    override def compareTo(o: TrackFollowing): Int = linearPosition - o.linearPosition match {
      case d if d > 0 => 1
      case d if d < 0 => -1
      case _          => 0
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
   * Adds to the [[Entity]] the ability to see a [[Balloon]] within his sight range.
   */
  trait SightAbility extends Entity {
    def sightRange: Double
    def direction: Vector2D

    def rotateTo(dir: Vector2D): SightAbility
    def sight(radius: Double): SightAbility
    def isInSightRange(balloon: Balloon): Boolean = position.intersectsWith(balloon)(sightRange)

    def canSee(balloon: Balloon): Boolean = balloon match {
      case CamoBalloon(_)          => false
      case BalloonDecoration(b, _) => canSee(b)
      case _                       => isInSightRange(balloon)
    }
  }

  /**
   * Adds to the [[Entity]] the ability to see even a [[CamoBalloon]] within its sight range.
   */
  trait EnhancedSightAbility extends SightAbility {
    override def canSee(balloon: Balloon): Boolean = isInSightRange(balloon)
  }

  /**
   * Adds to the [[Entity]] the ability to shoot a [[Bullet]].
   */
  trait ShotAbility extends Entity {
    def bullet: Bullet
    def shotRatio: Double

    def ratio(ratio: Double): ShotAbility

    def damage(ammo: Bullet): ShotAbility

    def canAttackAfter: Double => Boolean =
      lastShotTime => (System.currentTimeMillis() - lastShotTime) / 1000.0 >= shotRatio

    def canShootAfter: Double => Boolean =
      _ >= shotRatio

  }

}
