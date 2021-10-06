package model.spawn

import model.entities.balloons.BalloonDecorations.{ BalloonType, Regenerating }
import model.entities.balloons.BalloonLives.{ BalloonLife, Blue, Red }
import model.spawn.SpawnerMonad.add

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

object SpawnManager {

  case class BalloonInfo(balloonTypes: List[BalloonType] = List(), balloonLife: BalloonLife = Red)

  implicit class RichBalloonInfo(info: BalloonInfo) {

    def &(balloonType: BalloonType): BalloonInfo =
      BalloonInfo(balloonType :: info.balloonTypes, info.balloonLife)
  }

  case class Streak(
      quantity: Int = 1,
      balloonInfo: BalloonInfo = BalloonInfo(),
      interval: FiniteDuration = 100.milliseconds)

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
