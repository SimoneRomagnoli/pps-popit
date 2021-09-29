package model.spawn

import model.entities.balloons.BalloonType.{ BalloonLife, BalloonType }

import scala.concurrent.duration.FiniteDuration

object SpawnManager {

  case class Streak(
      balloonType: BalloonType,
      balloonLife: BalloonLife,
      quantity: Int,
      interval: FiniteDuration)

  case class Round(streaks: Seq[Streak])

}
