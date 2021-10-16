package model.stats

/**
 * Wrapper of game policies. It contains the life points and money won by popping balloons.
 */
object Stats {

  val startingLife: Int = 100
  val startingWallet: Int = 500
  val startingPoints: Int = 0

  trait GameStats {
    def life: Int
    def wallet: Int
    def gain(money: Int): Unit
    def pay(money: Int): Unit
    def lose(lifePoints: Int): Unit
  }

  object GameStats {

    def apply(life: Int = startingLife, wallet: Int = startingWallet): GameStats =
      GameStatistics(life, wallet)
  }

  case class GameStatistics(var life: Int, var wallet: Int) extends GameStats {
    override def gain(money: Int): Unit = wallet += money

    override def pay(money: Int): Unit = wallet -= money

    override def lose(lifePoints: Int): Unit = life -= lifePoints
  }
}
