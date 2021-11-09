package model.entities

import model.Positions.Vector2D
import model.Positions._
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.bullets.Bullets.Bullet
import model.maps.Tracks.Directions.{ Down, Up }
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

    /**
     * @return
     *   The boundary dimensions of the [[Entity]].
     */
    def boundary: Boundary

    /**
     * @return
     *   The current position of the [[Entity]].
     */
    def position: Vector2D

    /**
     * @param position
     *   The [[Vector2D]] representing the new position.
     * @return
     *   The current [[Entity]] in the new position.
     */
    def in(position: Vector2D): Entity

    /**
     * @param dt
     *   The time elapsed from last update.
     * @return
     *   The updated [[Entity]].
     */
    def update(dt: Double): Entity = this
  }

  /**
   * Adds to the [[Entity]] the ability to move:
   *   - has a speed
   *   - can change its speed
   *   - moves when updating
   */
  trait MovementAbility extends Entity {

    /**
     * @return
     *   The current speed of the [[Entity]].
     */
    def speed: Vector2D

    /**
     * @param speed
     *   The [[Vector2D]] representing the new speed.
     * @return
     *   The current [[Entity]] at the new speed.
     */
    def at(speed: Vector2D): Entity
    protected def move(dt: Double): Entity = this in (position + (speed * dt))

    abstract override def update(dt: Double): Entity =
      super.update(dt).asInstanceOf[MovementAbility] move dt
  }

  /**
   * Adds to the [[Entity]] the ability to follow a [[Track]]:
   *   - has a [[Track]]
   *   - can change the [[Track]]
   *   - can follow another [[TrackFollowing]]
   *   - moves on the track when updating
   */
  trait TrackFollowing extends MovementAbility with Comparable[TrackFollowing] { balloon: Balloon =>
    private var linearPosition: Double = 0.0

    /**
     * @return
     *   The [[Track]] the [[Entity]] is currently on.
     */
    def track: Track

    /**
     * @param track
     *   The new [[Track]].
     * @return
     *   The current [[Entity]] on the new [[Track]].
     */
    def on(track: Track): TrackFollowing

    /**
     * @param trackFollowing
     *   The other [[TrackFollowing]] to follow.
     * @return
     *   The current [[Entity]] following the other [[TrackFollowing]].
     */
    def following(trackFollowing: TrackFollowing): Balloon =
      following(trackFollowing.linearPosition).asInstanceOf[Balloon]

    private def following(lp: Double): TrackFollowing = {
      this.linearPosition = lp
      this
    }

    /** Sorts the [[Entity]]s according to their position on the track. */
    override def compareTo(o: TrackFollowing): Int = linearPosition - o.linearPosition match {
      case d if d > 0 => 1
      case d if d < 0 => -1
      case _          => 0
    }

    override protected def move(dt: Double): Entity = {
      linearPosition += (track directionIn linearPosition match {
        case Up | Down => speed.y
        case _         => speed.x
      }) * dt
      this
        .in(track exactPositionFrom linearPosition)
        .asInstanceOf[TrackFollowing]
        .following(linearPosition)
    }
  }

  /**
   * Adds to the [[Entity]] the ability to be popped. It also introduces the concept of life, which
   * represents the amount of times it has to be hit in order to pop.
   */
  trait PoppingAbility extends Entity {

    /**
     * @return
     *   The current life of the [[Entity]].
     */
    def life: Int

    /**
     * @param bullet
     *   The [[Bullet]] that hit the [[Entity]].
     * @return
     *   [[None]] if the [[Entity]] has no life left, otherwise an [[Option]] containing the popped
     *   [[Entity]].
     */
    def pop(bullet: Bullet): Option[Entity]
  }

  /**
   * Adds to the [[Entity]] the ability to see a [[Balloon]] within his sight range.
   */
  trait SightAbility extends Entity {
    def sightRange: Double

    def sight(radius: Double): SightAbility
    def isInSightRange(entity: Entity): Boolean = position.intersectsWith(entity)(sightRange)

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
   * Adds to the [[Entity]] the ability to rotate towards a direction.
   */
  trait RotationAbility extends Entity {
    def direction: Vector2D

    def rotateTo(dir: Vector2D): RotationAbility
  }

  /**
   * Adds to the [[Entity]] the ability to shoot a [[Bullet]].
   */
  trait ShootingAbility extends Entity {
    def bullet: Bullet
    def shotRatio: Double

    def ratio(ratio: Double): ShootingAbility

    def damage(ammo: Bullet): ShootingAbility

    def canAttackAfter: Double => Boolean =
      lastShotTime => (System.currentTimeMillis() - lastShotTime) / 1000.0 >= shotRatio

    def canShootAfter: Double => Boolean =
      _ >= shotRatio

  }

}
