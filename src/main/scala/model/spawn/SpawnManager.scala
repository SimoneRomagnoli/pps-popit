package model.spawn

import model.entities.balloons.BalloonLives.BalloonLife
import model.entities.balloons.BalloonTypes.BalloonType

import scala.concurrent.duration.FiniteDuration

object SpawnManager {

  case class Streak(
      balloonType: BalloonType,
      balloonLife: BalloonLife,
      quantity: Int,
      interval: FiniteDuration)

  case class Round(streaks: Seq[Streak])

}
