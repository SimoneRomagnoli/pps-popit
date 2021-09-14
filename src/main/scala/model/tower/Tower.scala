package model.tower

import model.tower.Position.Position

import scala.language.{implicitConversions, postfixOps}

object Position {

  def apply(x: Double, y: Double): Position = {
    (x,y)
  }

  case class Position(var x: Double, var y: Double) {
    def setX(v: Double): Unit = x = v
    def setY(v: Double): Unit = y = v

    def +(pos: Position): Position = (x + pos.x, y + pos.y)
    def *(value: Double): Position = (x * value, y * value)

    def distance(to: Position): Double = Math.sqrt(Math.pow(x - to.x, 2) + Math.pow(y - to.y, 2))

    def vector(to: Position): Position = (to.x - x, to.y - y)

  }

  implicit def toPosition(tuple: (Double, Double)): Position = Position(tuple._1, tuple._2)

}

object Tower {

  trait Tower {
    /*
    * TODO: add shot ratio and aim direction
    * */

    def position: Position
    def sightRadius: Double

    def move(to: Position): Unit
    def in(pos: Position): Tower
    def coverRange(radius: Double): Tower
    def intersects(position: Position, radius: Double): Boolean
  }

  trait CollisionBox extends Tower {
     override def intersects(pos: Position, radius: Double): Boolean = {
      (position distance pos) < (radius + sightRadius)
    }
  }

  class SimpleTower() extends Tower with CollisionBox {

    var currentPosition: Position = (0.0, 0.0)
    var sightRange: Double = 0.0

    override def in(pos: Position): Tower = {
      currentPosition = pos
      this
    }

    override def coverRange(radius: Double): Tower = {
      sightRange = radius
      this
    }

    override def move(to: Position): Unit = {
      currentPosition setX to.x
      currentPosition setY to.y
    }

    override def position: Position = currentPosition

    override def sightRadius: Double = sightRange

  }

  object Tower {

    def apply(position: Position, range: Double = 1.0): Tower = {
      new SimpleTower() in position coverRange range
    }

    implicit def fromPosition(position: Position): Tower = Tower(position)

  }

}
