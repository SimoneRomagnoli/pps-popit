package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.Regenerating
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

import scala.concurrent.duration.DurationInt

object RoundsFactory {
  private var round: Int = 14

  private val easyRoundLimit: Int = 15
  private val regeneratingRoundLimit: Int = 20

  def currentRound: Int = round

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- chooseRound(round)
  } yield ()).get

  private def chooseRound(round: Int): IO[Unit] = round match {
    case i if i < easyRoundLimit => easyRounds(round)
    case _                       => regeneratingRounds(round % easyRoundLimit + 1)
  }

  private def easyRounds(round: Int): IO[Unit] = for {
    _ <- add(
      (Streak(
        if (round <= 7) 5 * round
        else 5 * (easyRoundLimit - round)
      ) :- Red) @@ (1000 / (round % 5 + 1)).milliseconds
    )
    _ <-
      if (round >= 5) add((Streak(round) :- Blue) @@ (1000 / (round % 5 + 1)).milliseconds)
      else IO.unit
    _ <-
      if (round >= 10) add((Streak(round / 2) :- Green) @@ (1000 / (round % 5 + 1)).milliseconds)
      else IO.unit
  } yield ()

  private def regeneratingRounds(round: Int): IO[Unit] = for {
    _ <- easyRounds(round * 3)
    _ <- add((Streak(round * 3) :- (Red & Regenerating)) @@ 300.milliseconds)
    _ <- add((Streak(round * 2) :- (Blue & Regenerating)) @@ 300.milliseconds)
    _ <- add((Streak(round) :- (Green & Regenerating)) @@ 300.milliseconds)
  } yield ()

  private def incrementRound(): IO[Unit] = IO(round += 1)
}
