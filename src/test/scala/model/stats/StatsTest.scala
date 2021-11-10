package model.stats

import model.stats.Stats.GameStats
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StatsTest extends AnyWordSpec with Matchers {
  var stats: GameStats = GameStats()
  val amount: Int = 10

  "The Stats" when {
    "just created" should {
      "have default behaviour" in {
        stats.wallet shouldBe Stats.startingWallet
        stats.life shouldBe Stats.startingLife
        stats.round shouldBe Stats.startingRound
      }
    }
    "modified" should {
      "change its values" in {
        stats = stats pay amount
        stats.wallet shouldBe Stats.startingWallet - amount
        stats = stats gain amount
        stats.wallet shouldBe Stats.startingWallet
        stats = stats lose amount
        stats.life shouldBe Stats.startingLife - amount
        stats = stats.updateRound(1)
        stats.round shouldBe Stats.startingRound + 1
      }
    }
  }

}
