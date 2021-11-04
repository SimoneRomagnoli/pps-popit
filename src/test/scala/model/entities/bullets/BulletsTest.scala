package model.entities.bullets

import commons.CommonValues
import model.Positions.{ defaultPosition, fromTuple }
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues._
import model.entities.bullets.Bullets.{ shoot, CannonBall, Dart, IceBall }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class BulletsTest extends AnyFlatSpec with Matchers {
  val iceBall: IceBall = IceBall(bulletDefaultRadius, bulletFreezingTime)
  val dart: Dart = Dart()
  val cannonBall: CannonBall = CannonBall(bulletDefaultRadius)
  val balloon: Balloon = (Red balloon) in (100.0, 100.0)

  "A dart" should "have default position, speed, damage and toString" in {
    dart.position shouldBe defaultPosition
    dart.speed shouldBe bulletDefaultSpeed
    dart.damage shouldBe bulletDefaultDamage
    dart.toString shouldBe "DART"
  }

  "A cannonBall" should "be a Dart with also a radius for the explosion" in {
    cannonBall.position shouldBe defaultPosition
    cannonBall.speed shouldBe bulletDefaultSpeed
    cannonBall.damage shouldBe bulletDefaultDamage
    cannonBall.radius shouldBe bulletDefaultRadius

    cannonBall.toString shouldBe "CANNON-BALL"
  }

  "An iceBall" should "be a cannonBall with also a freezingTime" in {
    iceBall.position shouldBe defaultPosition
    iceBall.speed shouldBe bulletDefaultSpeed
    iceBall.damage shouldBe bulletDefaultDamage
    iceBall.radius shouldBe bulletDefaultRadius
    iceBall.freezingTime shouldBe bulletFreezingTime
    iceBall.toString shouldBe "ICE-BALL"
  }

  "Dart, cannonBall and iceBall" should " be shoot" in {
    shoot(dart).isInstanceOf[Dart] shouldBe true
    shoot(cannonBall).isInstanceOf[CannonBall] shouldBe true
    shoot(iceBall).isInstanceOf[IceBall] shouldBe true
  }

  "The bullet damage" should "be able to be powered up" in {
    val newDamage: Double = 3.0
    dart.hurt(newDamage)
    dart.damage shouldBe newDamage
  }

  "A bullet" should "be able to move" in {
    (dart at (2.0, 2.0)).update(5.0).position shouldBe fromTuple((10.0, 10.0))
  }

  "A bullet" should "collide with a ballon" in {
    dart in (0.0, 0.0)
    dart at (100.0, 100.0)
    dart hit balloon shouldBe false
    dart.update(1.0)
    dart hit balloon shouldBe true
  }

  "A bullet" should "recognize when it exit from the screen" in {
    dart in (0.0, 0.0)
    dart.exitedFromScreen() shouldBe false
    dart in (CommonValues.Screen.width + 1, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart in (0.0, CommonValues.Screen.height + 1)
    dart.exitedFromScreen() shouldBe true
    dart in (-1.0, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart in (0.0, -1.0)
    dart.exitedFromScreen() shouldBe true
  }
}
