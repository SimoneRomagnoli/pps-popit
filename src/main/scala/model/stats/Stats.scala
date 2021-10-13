package model.stats

/**
 * Wrapper of game policies. It contains the life points and money won by popping balloons.
 */
object Stats {

  val startingLife: Int = 100
  val startingWallet: Int = 500
  val startingPoints: Int = 0

  trait GameStats {
    def points: Int
    def score(score: Int): Unit
    def life: Int
    def wallet: Int
    def win(money: Int): Unit
    def spend(money: Int): Unit
    def lose(lifePoints: Int): Unit
  }

  object GameStats {

    def apply(
        life: Int = startingLife,
        wallet: Int = startingWallet,
        points: Int = startingPoints): GameStats =
      GameStatistics(life, wallet, points)
  }

  case class GameStatistics(var life: Int, var wallet: Int, var points: Int) extends GameStats {
    override def win(money: Int): Unit = wallet += money

    override def spend(money: Int): Unit = wallet -= money

    override def lose(lifePoints: Int): Unit = life -= lifePoints

    override def score(score: Int): Unit = points += score
  }
}
