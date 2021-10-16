package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.Regenerating
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

import scala.util.Random

object RoundsFactory {
  private var round: Int = 0

  def currentRound: Int = round

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- add(Streak(nBalloons(1)) :- Red)
    _ <- add(Streak(nBalloons(0.6)) :- Blue)
    _ <- add(Streak(nBalloons(0.4)) :- Green)
    _ <- if (round > 0) add(Streak(nBalloons(0.8)) :- (Red & Regenerating)) else IO.unit
  } yield ()).get

  private def incrementRound(): IO[Unit] = IO(round += 1)

  private val nBalloons: Double => Int = probability =>
    (Random.nextDouble() * round * probability * 10).toInt
}
