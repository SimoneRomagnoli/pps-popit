package model.entities.towers

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import model.Positions.Vector2D
import model.entities.balloons.Balloons.{ Balloon, Simple }
import model.entities.bullets.Bullets.Dart
import model.entities.towers.TowerTypes.Arrow
import model.entities.towers.Towers.Tower
import model.entities.towers.TowersTest._
import org.scalatest.wordspec.AnyWordSpecLike
import utils.Constants.Entities.Towers.towerDefaultShotRatio

import scala.language.postfixOps

object TowersTest {

  val balloonPosition: Vector2D = (6.0, 8.0)
  val balloonBoundary: (Double, Double) = (1.0, 1.0)
  val towerPosition: Vector2D = (2.0, 1.0)
  var lastShotTime: Double = 0.0

  val tower: Tower[Dart] =
    (Arrow tower) in towerPosition withSightRangeOf 1.0 withShotRatioOf towerDefaultShotRatio
  var balloon: Balloon = Simple(position = balloonPosition, boundary = balloonBoundary)

  def waitSomeTime(): Unit = Thread.sleep(500)
}

class TowersTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "During a simple game" when {
    "tower and balloon has just spawned, the tower" should {
      "not see the balloon because it is across the map" in {
        (tower position) shouldBe towerPosition
        (balloon position) shouldBe balloonPosition
        tower canSee balloon shouldBe false
      }
    }
    "the balloon goes near the tower, it" should {
      "see the balloon because it is in tower's sight range" in {
        balloon = balloon in ((3.0, 2.0))
        (tower position) shouldBe towerPosition
        balloon.position.x should be < balloonPosition.x
        balloon.position.y should be < balloonPosition.y
        tower canSee balloon shouldBe true
      }
    }
    "the tower can see the balloon, it" should {
      "shot the balloon with the specified shot ratio" in {
        (tower shotRatio) shouldBe towerDefaultShotRatio
        tower canAttackAfter lastShotTime shouldBe true
        lastShotTime = System.currentTimeMillis().toDouble
        tower canAttackAfter lastShotTime shouldBe false
        tower canAttackAfter lastShotTime shouldBe false
        waitSomeTime()
        tower canAttackAfter lastShotTime shouldBe true
      }
      "can change the shot frequency according to the shot ratio" in {
        lastShotTime = 0.0
        val newTower: Tower[Dart] = tower withShotRatioOf 1.0
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
