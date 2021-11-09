package model.spawn

import cats.effect.IO
import model.entities.balloons.BalloonDecorations.{ BalloonType, Camo, Lead, Regenerating }
import model.entities.balloons.BalloonLives.{ Blue, Green, Red }
import model.spawn.BalloonsFunctions._
import model.spawn.RoundBuilders.RoundBuilder.define
import model.spawn.RoundBuilders.{ add, RichIO }
import model.spawn.Rounds.{ Round, Streak }

import scala.concurrent.duration.DurationInt

/** Game rounds logic: a suitable round is built according to the current progress. */
object RoundsFactory {
  private var round: Int = 0

  def currentRound: Int = round

  def startGame(): Unit = round = 0

  def nextRound(): Round = (for {
    _ <- incrementRound()
    _ <- define()
    _ <- chooseRound(round)
  } yield ()).built

  private def chooseRound(round: Int): IO[Unit] = round match {
    case i if i < easyRoundLimit => easyRounds(round)
    case i if i < regeneratingRoundLimit =>
      decorationRounds(round % easyRoundLimit + 1)(Regenerating)
    case i if i < leadRoundLimit => decorationRounds(round % regeneratingRoundLimit + 1)(Lead)
    case i if i < camoRoundLimit => decorationRounds(round % leadRoundLimit + 1)(Camo)
    case i if i < regeneratingLeadRoundLimit =>
      decorationRounds(round % camoRoundLimit + 1)(List(Regenerating, Camo))
    case _ =>
      decorationRounds(round % regeneratingLeadRoundLimit + 1)(List(Regenerating, Camo, Lead))
  }

  private def easyRounds(round: Int): IO[Unit] = for {
    _ <- add((Streak(redBalloonQuantity(round)) :- Red) @@ balloonsDelay(round).milliseconds)
    _ <- add((Streak(blueBalloonQuantity(round)) :- Blue) @@ balloonsDelay(round).milliseconds)
    _ <- add((Streak(greenBalloonQuantity(round)) :- Green) @@ balloonsDelay(round).milliseconds)
  } yield ()

  private def decorationRounds(round: Int)(decorations: List[BalloonType]): IO[Unit] = for {
    _ <- easyRounds(round * 3)
    _ <- add(Streak(round * 3) :- (Red & decorations))
    _ <- add(Streak(round * 2) :- (Blue & decorations))
    _ <- add(Streak(round) :- (Green & decorations))
  } yield ()

  private def incrementRound(): IO[Unit] = IO(round += 1)
}

/**
 * The quantity of balloons to spawn is calculated through piecewise-defined functions based on the
 * current round.
 */
object BalloonsFunctions {
  val easyRoundLimit: Int = 15
  val regeneratingRoundLimit: Int = 20
  val leadRoundLimit: Int = 25
  val camoRoundLimit: Int = 30
  val regeneratingLeadRoundLimit: Int = 35

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
