package model.stats

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

  case class GameStatistics(override val life: Int, override val wallet: Int) extends GameStats {
    override def win(money: Int): Unit = GameStats(life, wallet + money)

    override def spend(money: Int): Unit = GameStats(life, wallet - money)

    override def lose(lifePoints: Int): Unit = GameStats(life - lifePoints, wallet)
  }
}
