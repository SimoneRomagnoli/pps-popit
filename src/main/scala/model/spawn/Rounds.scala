package model.spawn

import model.entities.balloons.BalloonDecorations.BalloonType
import model.entities.balloons.BalloonLives.{BalloonLife, Red}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * Definition of the game rounds and of the DSL to define them: a round is made up of multiple
 * streaks containing an arbitrary amount of a certain type of
 * [[model.entities.balloons.Balloons.Balloon]].
 *
 * A round can thus be defined as following:
 * {{{
 * Round.of(
 *   Streak(n) :- balloonLife,
 *   Streak(m) :- (balloonLife & balloonType1 & balloonType2 & ...),
 *   (Streak(l) :- (balloonLife & balloonType1 & balloonType2 & ...) @@ delay),
 * )
 * }}}
 */
object Rounds {

  /** Wrapper for [[BalloonType]]s and [[BalloonLife]]. */
  case class BalloonInfo(balloonTypes: List[BalloonType] = List(), balloonLife: BalloonLife = Red)

  implicit class RichBalloonInfo(info: BalloonInfo) {

    def &(balloonType: BalloonType): BalloonInfo =
      BalloonInfo(balloonType :: info.balloonTypes, info.balloonLife)
  }

  /**
   * A [[Streak]] is a "wave" of n [[model.entities.balloons.Balloons.Balloon]] s with a
   * [[BalloonInfo]] spawned at a fixed ratio.
   * @param quantity
   *   The number of [[model.entities.balloons.Balloons.Balloon]] s to be spawned.
   * @param balloonInfo
   *   The info about the [[model.entities.balloons.Balloons.Balloon]] including the [[List]] of
   *   [[BalloonType]] and the [[BalloonLife]].
   * @param interval
   *   The time elapsed between the spawn of a [[model.entities.balloons.Balloons.Balloon]] and the
   *   next one.
   */
  case class Streak(
      quantity: Int = 1,
      balloonInfo: BalloonInfo = BalloonInfo(),
      interval: FiniteDuration = 300.milliseconds)

  /** A round of the game that is made up of a [[Seq]] of [[Streak]]. */
  case class Round(streaks: Seq[Streak])

  implicit class RichStreak(s: Streak) {

    def :-(balloonInfo: BalloonInfo): Streak = s match {
      case Streak(quantity, _, interval) =>
        Streak(quantity, balloonInfo, interval)
    }

    def :-(balloonLife: BalloonLife): Streak = s match {
      case Streak(quantity, balloonInfo, interval) =>
        Streak(quantity, BalloonInfo(balloonInfo.balloonTypes, balloonLife), interval)
    }

    def @@(interval: FiniteDuration): Streak = s match {
      case Streak(quantity, balloonInfo, _) =>
        Streak(quantity, balloonInfo, interval)
    }
  }

}
