package model

import scala.language.{implicitConversions, postfixOps}

object Positions {

  sealed trait Vector2D {
    def x: Double
    def y: Double
  }
  case class Vector2DImpl(override val x: Double, override val y: Double) extends Vector2D

  implicit class Positions(v: Vector2D) {
    def +(other: Vector2D): Vector2D = (v.x + other.x, v.y + other.y)
    def *(value: Double): Vector2D = (v.x * value, v.y * value)
  }

  implicit def toVector(t: (Double, Double)): Vector2DImpl = Vector2DImpl(t._1, t._2)

}
