package model.maps

import model.maps.StatsTest.{ amount, stats }
import model.stats.Stats
import model.stats.Stats.GameStats
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

object StatsTest {
  val stats: GameStats = GameStats()
  val amount: Int = 10
}

class StatsTest extends AnyWordSpec with Matchers {

  "The Stats" when {
    "just created" should {
      "have default behaviour" in {
        stats.wallet shouldBe Stats.startingWallet
        stats.life shouldBe Stats.startingLife
        stats.points shouldBe Stats.startingPoints
      }
    }
    "modified" should {
      "change its values" in {
        stats spend amount
        stats.wallet shouldBe Stats.startingWallet - amount
        stats win amount
        stats.wallet shouldBe Stats.startingWallet
        stats lose amount
        stats.life shouldBe Stats.startingLife - amount
        stats score amount
        stats.points shouldBe Stats.startingPoints + amount
      }
    }
  }

}
