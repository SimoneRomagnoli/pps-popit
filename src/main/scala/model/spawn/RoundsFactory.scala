package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

object RoundsFactory {
  private var round: Int = 0

  def currentRound: Int = round

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- add(Streak(round * 10) :- Red)
    _ <- add(Streak(round * 10) :- Blue)
    _ <- add(Streak(round * 5) :- Green)
  } yield ()).get

  private def incrementRound(): IO[Unit] = IO(round += 1)
}
