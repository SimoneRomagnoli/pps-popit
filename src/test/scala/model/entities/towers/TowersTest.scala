package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.Positions.Vector2D
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.bullets.Bullets.{ Bullet, Dart }
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.TowerValues.towerDefaultShotRatio
import model.entities.towers.Towers.Tower
import model.entities.towers.TowersTest._
import org.scalatest.wordspec.AnyWordSpecLike

import scala.language.postfixOps

object TowersTest {

  val balloonPosition: Vector2D = (6.0, 8.0)
  val balloonBoundary: (Double, Double) = (1.0, 1.0)
  val towerPosition: Vector2D = (2.0, 1.0)
  var lastShotTime: Double = 0.0

  val tower: Tower[Dart] =
    ((Arrow tower) in towerPosition) sight 1.0 ratio towerDefaultShotRatio
  var balloon: Balloon = Simple(position = balloonPosition, boundary = balloonBoundary)

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class TowersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Towers" when {
    "just created" should {
      "not see the balloon because it is across the map" in {
        (tower position) shouldBe towerPosition
        (balloon position) shouldBe balloonPosition
        tower canSee balloon shouldBe false

        val arrow: Tower[Bullet] = TowerTypes.arrow spawn

        arrow.bullet.isInstanceOf[Dart] shouldBe true
      }
    }
    "the balloon is nearing" should {
      "see the balloon because it is in tower's sight range" in {
        balloon = balloon in ((3.0, 2.0))
        (tower position) shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower canSee balloon shouldBe true
      }
    }
    "are able to see the balloon" should {
      "shoot the balloon with the specified shot ratio" in {
        (tower shotRatio) shouldBe towerDefaultShotRatio
        tower canAttackAfter lastShotTime shouldBe true
        lastShotTime = System.currentTimeMillis().toDouble
        tower canAttackAfter lastShotTime shouldBe false
        tower canAttackAfter lastShotTime shouldBe false
        waitSomeTime()
        tower canAttackAfter lastShotTime shouldBe true
      }
      "be able to change the shot frequency according to the shot ratio" in {
        lastShotTime = 0.0
        val newTower: Tower[Dart] = tower ratio 1.0
        (newTower shotRatio) shouldBe 1.0
        newTower canAttackAfter lastShotTime shouldBe true
        lastShotTime = System.currentTimeMillis().toDouble
        newTower canAttackAfter lastShotTime shouldBe false
        waitSomeTime()
        newTower canAttackAfter lastShotTime shouldBe false
        waitSomeTime()
        newTower canAttackAfter lastShotTime shouldBe true
      }
    }
  }

}
