package model.stats

/**
 * Wrapper of game policies. It contains the life points and money won by popping balloons.
 */
object Stats {

  val startingLife: Int = 100
  val startingWallet: Int = 500

  trait GameStats {
    def life: Int
    def wallet: Int
    def win(money: Int): Unit
    def spend(money: Int): Unit
    def lose(lifePoints: Int): Unit
  }

  object GameStats {

    def apply(life: Int = startingLife, wallet: Int = startingWallet): GameStats =
      GameStatistics(life, wallet)
  }

  case class GameStatistics(var life: Int, var wallet: Int) extends GameStats {
    override def win(money: Int): Unit = wallet += money

    override def spend(money: Int): Unit = wallet -= money

    override def lose(lifePoints: Int): Unit = life -= lifePoints
  }
}
