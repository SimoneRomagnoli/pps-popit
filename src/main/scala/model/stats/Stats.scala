package model.stats

/**
 * Wrapper of game policies. It contains the life points and money won by popping balloons.
 */
object Stats {

  val startingLife: Int = 100
  val startingWallet: Int = 15000
  val startingRound: Int = 0

  trait GameStats {
    def life: Int
    def wallet: Int
    def round: Int
    def gain(money: Int): Unit
    def pay(money: Int): Unit
    def lose(lifePoints: Int): Unit
    def nextRound(): Unit
  }

  object GameStats {

    def apply(
        life: Int = startingLife,
        wallet: Int = startingWallet,
        round: Int = startingRound): GameStats =
      GameStatistics(life, wallet, round)
  }

  case class GameStatistics(var life: Int, var wallet: Int, var round: Int) extends GameStats {
    override def gain(money: Int): Unit = wallet += money
    override def pay(money: Int): Unit = wallet -= money
    override def lose(lifePoints: Int): Unit = life -= lifePoints
    override def nextRound(): Unit = round += 1
  }
}
