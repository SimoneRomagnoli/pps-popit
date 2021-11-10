package model.stats

/**
 * Wrapper of game policies. It contains the round number, life points and money won by popping
 * balloons.
 */
object Stats {

  val startingLife: Int = 10
  val startingWallet: Int = 200
  val startingRound: Int = 0

  trait GameStats {
    def life: Int
    def wallet: Int
    def round: Int
    def gain(money: Int): GameStats
    def pay(money: Int): GameStats
    def lose(lifePoints: Int): GameStats
    def updateRound(round: Int): GameStats
  }

  object GameStats {

    def apply(
        life: Int = startingLife,
        wallet: Int = startingWallet,
        round: Int = startingRound): GameStats =
      GameStatistics(life, wallet, round)
  }

  case class GameStatistics(var life: Int, var wallet: Int, var round: Int) extends GameStats {
    override def gain(money: Int): GameStats = GameStatistics(life, wallet + money, round)
    override def pay(money: Int): GameStats = GameStatistics(life, wallet - money, round)
    override def lose(lifePoints: Int): GameStats = GameStatistics(life - lifePoints, wallet, round)
    override def updateRound(r: Int): GameStats = GameStatistics(life, wallet, r)
  }
}
