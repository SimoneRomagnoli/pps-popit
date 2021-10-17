package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.{ Camo, Regenerating }
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

object RoundsFactory {
  private var round: Int = 0

  def currentRound: Int = round

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- add(Streak(round * 5) :- (Red & Camo))
    _ <- add(Streak(round * 5) :- (Blue & Camo))
    _ <- add(Streak(round * 5) :- (Green & Camo))
    _ <- add(Streak(round * 5) :- (Red & Regenerating))
    _ <- add(Streak(round * 5) :- (Blue & Regenerating))
    _ <- add(Streak(round * 5) :- (Green & Regenerating))
  } yield ()).get

  private def incrementRound(): IO[Unit] = IO(round += 1)
}
