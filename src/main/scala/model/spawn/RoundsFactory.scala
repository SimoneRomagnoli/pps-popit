package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.{ Camo, Lead, Regenerating }
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

object RoundsFactory {
  private var round: Int = 0

  def currentRound: Int = round

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- add(Streak(round) :- (Red & Lead))
    _ <- add(Streak(round) :- (Blue & Lead))
    _ <- add(Streak(round) :- (Green & Lead))
    _ <- add(Streak(round) :- (Red & Lead & Regenerating & Camo))
    _ <- add(Streak(round) :- (Blue & Lead & Regenerating & Camo))
    _ <- add(Streak(round) :- (Green & Lead & Regenerating & Camo))
  } yield ()).get

  private def incrementRound(): IO[Unit] = IO(round += 1)
}
