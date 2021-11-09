package model.entities.bullets

import commons.CommonValues
import model.Positions.{ defaultPosition, fromTuple }
import model.entities.balloons.BalloonLives.Red
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.BulletValues._
import model.entities.bullets.Bullets.{ Bullet, CannonBall, Dart, IceBall, Shooting }
import model.entities.towers.TowerTypes.{ Arrow, Cannon, Ice }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class BulletsTest extends AnyFlatSpec with Matchers {
  val iceBall: IceBall = IceBall()
  var dart: Bullet = Dart()
  val cannonBall: CannonBall = CannonBall()
  val balloon: Balloon = (Red balloon) in (100.0, 100.0)

  "A Dart" should "have default position, speed, damage and toString" in {
    dart.position shouldBe defaultPosition
    dart.speed shouldBe bulletDefaultSpeed
    dart.damage shouldBe bulletDefaultDamage
    dart.toString shouldBe "DART"
  }

  "A CannonBall" should "be a Dart with also a radius for the explosion" in {
    cannonBall.position shouldBe defaultPosition
    cannonBall.speed shouldBe bulletDefaultSpeed
    cannonBall.damage shouldBe bulletDefaultDamage
    cannonBall.sightRange shouldBe bulletDefaultSightRange

    cannonBall.toString shouldBe "CANNON-BALL"
  }

  "An IceBall" should "be a cannonBall with also a freezingTime" in {
    iceBall.position shouldBe defaultPosition
    iceBall.speed shouldBe bulletDefaultSpeed
    iceBall.damage shouldBe bulletDefaultDamage
    iceBall.sightRange shouldBe bulletDefaultSightRange
    iceBall.freezingTime shouldBe bulletFreezingTime
    iceBall.toString shouldBe "ICE-BALL"
  }

  "Dart, CannonBall and IceBall" should "be shot" in {

    (Shooting from (Arrow tower)).isInstanceOf[Dart] shouldBe true
    (Shooting from (Cannon tower)).isInstanceOf[CannonBall] shouldBe true
    (Shooting from (Ice tower)).isInstanceOf[IceBall] shouldBe true
  }

  "The Bullet Damage" should "be able to be powered up" in {
    val newDamage: Double = 3.0
    dart = dart.hurt(newDamage)
    dart.damage shouldBe newDamage
  }

  "A Bullet" should "be able to move" in {
    (dart at (2.0, 2.0)).update(5.0).position shouldBe fromTuple((10.0, 10.0))
  }

  "A Bullet" should "collide with a balloon" in {
    dart = dart in (0.0, 0.0)
    dart = dart at (100.0, 100.0)
    dart hit balloon shouldBe false
    dart = dart.update(1.0).asInstanceOf[Bullet]
    dart hit balloon shouldBe true
  }

  "A Bullet" should "realise when it goes out of the screen" in {
    dart = dart in (0.0, 0.0)
    dart.exitedFromScreen() shouldBe false
    dart = dart in (CommonValues.Screen.width + 1, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart = dart in (0.0, CommonValues.Screen.height + 1)
    dart.exitedFromScreen() shouldBe true
    dart = dart in (-1.0, 0.0)
    dart.exitedFromScreen() shouldBe true
    dart = dart in (0.0, -1.0)
    dart.exitedFromScreen() shouldBe true
  }
}
