package model.entities

import model.Positions.Vector2D

object Entities {

  trait Entity {
    def position: Vector2D
    def in(position: Vector2D): Entity
    def update(dt: Double): Entity = this
  }



}
