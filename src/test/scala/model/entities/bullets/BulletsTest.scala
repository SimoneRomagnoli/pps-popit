package model.entities.bullets

import model.Positions.{ fromTuple, Vector2D }
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BulletsTest extends AnyFlatSpec with Matchers {
  val position: Vector2D = (0.0, 0.0)
  val speed: Vector2D = (1.0, 1.0)
  val damage: Double = 1.0
  val radius: Double = 2.0
  val freezingTime: Double = 1.0
  val boundary: (Double, Double) = (2.0, 1.0)

  val iceBall: IceBall = IceBall(damage, position, speed, radius, freezingTime, boundary)

  val dart: Dart = Dart(damage, position, speed, boundary)
  val cannonBall: CannonBall = CannonBall(damage, position, speed, radius, boundary)

  "A Dart" should "have default position, speed and damage" in {
    dart.position shouldBe position
    dart.speed shouldBe speed
    dart.damage shouldBe damage
  }

  "A CannonBall" should "be a Dart with also a radius for the explosion" in {
    cannonBall.position shouldBe position
    cannonBall.speed shouldBe speed
    cannonBall.damage shouldBe damage
    cannonBall.radius shouldBe radius
  }

  "An IceBall" should "be a cannonBall with also a freezingTime" in {
    iceBall.position shouldBe position
    iceBall.speed shouldBe speed
    iceBall.damage shouldBe damage
    iceBall.radius shouldBe radius
    iceBall.freezingTime shouldBe freezingTime
  }

  it should "be able to move" in {
    (dart at (2.0, 2.0)).update(5.0).position shouldBe fromTuple((10.0, 10.0))
  }
}
