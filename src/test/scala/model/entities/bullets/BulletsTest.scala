package model.entities.bullets

import model.Positions.{ fromTuple, Vector2D }
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.Commons
import model.Positions.defaultPosition
import model.entities.bullets.BulletValues._

import scala.language.postfixOps

class BulletsTest extends AnyFlatSpec with Matchers {
  val position: Vector2D = defaultPosition
  val speed: Vector2D = bulletDefaultSpeed
  val damage: Double = bulletDefaultDamage
  val radius: Double = bulletDefaultRadius
  val freezingTime: Double = bulletFreezingTime
  val boundary: (Double, Double) = bulletDefaultBoundary

  val iceBall: IceBall = IceBall(radius, freezingTime)

  val dart: Dart = Dart()
  val cannonBall: CannonBall = CannonBall(radius)
  val balloon: Balloon = (Red balloon) in (100.0, 100.0)

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

  "A Dart" should "be able to move" in {
    (dart at (2.0, 2.0)).update(5.0).position shouldBe fromTuple((10.0, 10.0))
  }

  "A Dart" should "collide with a ballon" in {
    dart in (0.0, 0.0)
    dart at (100.0, 100.0)
    dart hit balloon shouldBe false
    dart.update(1.0)
    dart hit balloon shouldBe true
  }

  "A Dart" should "recognize when it exit from the screen" in {
    dart in (0.0, 0.0)
    dart.exitedFromScreen() shouldBe false
    dart in (Commons.Screen.width + 1, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart in (0.0, Commons.Screen.height + 1)
    dart.exitedFromScreen() shouldBe true
    dart in (-1.0, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart in (0.0, -1.0)
    dart.exitedFromScreen() shouldBe true
  }
}
