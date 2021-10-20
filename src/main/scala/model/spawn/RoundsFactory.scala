package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.Regenerating
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.BalloonsFunctions._
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

import scala.concurrent.duration.DurationInt

object RoundsFactory {
  private var round: Int = 0

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
    _ <- add((Streak(redBalloonQuantity(round)) :- Red) @@ balloonsDelay(round).milliseconds)
    _ <- add((Streak(blueBalloonQuantity(round)) :- Blue) @@ balloonsDelay(round).milliseconds)
    _ <- add((Streak(greenBalloonQuantity(round)) :- Green) @@ balloonsDelay(round).milliseconds)
  } yield ()

  private def regeneratingRounds(round: Int): IO[Unit] = for {
    _ <- easyRounds(round * 3)
    _ <- add(Streak(round * 3) :- (Red & Regenerating))
    _ <- add(Streak(round * 2) :- (Blue & Regenerating))
    _ <- add(Streak(round) :- (Green & Regenerating))
  } yield ()

  private def incrementRound(): IO[Unit] = IO(round += 1)
}

object BalloonsFunctions {
  val easyRoundLimit: Int = 15
  val regeneratingRoundLimit: Int = 20

  val balloonsDelay: Int => Int = x => 1000 / (x % 5 + 1)

  val redBalloonQuantity: Int => Int = {
    case x if x < easyRoundLimit / 2 => (easyRoundLimit / 3) * x
    case x                           => (easyRoundLimit / 3) * (easyRoundLimit - x)
  }

  val blueBalloonQuantity: Int => Int = {
    case x if x >= (easyRoundLimit / 3) => x
    case _                              => 0
  }

  val greenBalloonQuantity: Int => Int = {
    case x if x >= (easyRoundLimit / 3 * 2) => x / 2
    case _                                  => 0
  }
}
