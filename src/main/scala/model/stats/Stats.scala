package model.stats

/**
 * Wrapper of game policies. It contains the life points and money won by popping balloons.
 */
object Stats {

  val maxLife: Int = 100

  trait GameStats {
    def life: Int
    def wallet: Int
    def win(money: Int): Unit
    def spend(money: Int): Unit
    def lose(lifePoints: Int): Unit
  }

  object GameStats {

    def apply(life: Int = maxLife, wallet: Int = 0): GameStats =
      GameStatistics(life, wallet)
  }

  case class GameStatistics(var life: Int, var wallet: Int) extends GameStats {
    override def win(money: Int): Unit = wallet += money

    override def spend(money: Int): Unit = wallet -= money

    override def lose(lifePoints: Int): Unit = life -= lifePoints
  }
}
