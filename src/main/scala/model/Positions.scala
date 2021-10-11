package model

import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon

import scala.language.{ implicitConversions, postfixOps }

object Positions {

  /**
   * A point in 2D with x and y coordinates. It can be used to represent either position and speed
   * of an [[Entity]].
   */
  sealed trait Vector2D {
    def x: Double
    def y: Double
  }
  case class Vector2DImpl(override val x: Double, override val y: Double) extends Vector2D

  /**
   * Provides an easier way to do operations on a [[Vector2D]].
   * @param v:
   *   The [[Vector2D]] we want to operate with.
   */
  implicit class Positions(v: Vector2D) {
    def +(other: Vector2D): Vector2D = (v.x + other.x, v.y + other.y)
    def -(other: Vector2D): Vector2D = (v.x - other.x, v.y - other.y)
    def *(value: Double): Vector2D = (v.x * value, v.y * value)

    def intersectsWith(balloon: Balloon)(radius: Double): Boolean =
      distanceVector(v)(balloon position) match {
        case Vector2DImpl(x, _) if x > ((balloon.boundary._1 / 2) + radius) => false
        case Vector2DImpl(_, y) if y > ((balloon.boundary._2 / 2) + radius) => false
        case Vector2DImpl(x, _) if x <= (balloon.boundary._1 / 2)           => true
        case Vector2DImpl(_, y) if y <= (balloon.boundary._2 / 2)           => true
        case distance =>
          squareDistance(distance)((balloon.boundary._1 / 2, balloon.boundary._2 / 2)) <= Math.pow(
            radius,
            2
          )
      }
  }

  /**
   * Provides an easier way to define a [[Vector2D]] from a [[Tuple2]].
   * @param t:
   *   The [[Tuple2]].
   * @return
   *   the [[Vector2D]] corresponding to the given [[Tuple2]].
   */
  implicit def fromTuple(t: (Double, Double)): Vector2DImpl = Vector2DImpl(t._1, t._2)

  /**
   * Calculates the distance between two points.
   * @param from,
   *   first position
   * @param to,
   *   second position
   * @return
   */
  def distance(from: Vector2D)(to: Vector2D): Double =
    Math.sqrt(squareDistance(from)(to))

  def squareDistance(from: Vector2D)(to: Vector2D): Double =
    Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2)

  def distanceVector(from: Vector2D)(to: Vector2D): Vector2D =
    (Math.abs(from.x - to.x), Math.abs(from.y - to.y))

  def vector(from: Vector2D)(to: Vector2D): Vector2D = (to.x - from.x, to.y - from.y)

  def normalized(vector: Vector2D): Vector2D = {
    val magnitude = magnitudeOf(vector)
    (vector.x / magnitude, vector.y / magnitude)
  }

  def magnitudeOf(vector: Vector2D): Double = Math.sqrt(vector.x * vector.x + vector.y * vector.y)

}
