package model

import model.entities.Entities.Entity

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
  }

  /**
   * Provides an easier way to define a [[Vector2D]] from a [[Tuple2]].
   * @param t:
   *   The [[Tuple2]].
   * @return
   *   the [[Vector2D]] corresponding to the given [[Tuple2]].
   */
  implicit def toVector(t: (Double, Double)): Vector2DImpl = Vector2DImpl(t._1, t._2)

  /**
   * Calculates the distance between two points.
   * @param from
   * @param to
   * @return
   */
  def distance(from: Vector2D)(to: Vector2D): Double =
    Math.sqrt(Math.pow(from.x - to.x, 2) + Math.pow(from.y - to.y, 2))

}
