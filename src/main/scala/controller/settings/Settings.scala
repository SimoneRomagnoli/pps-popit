package controller.settings

import controller.settings.Settings.Time.TimeSettings

/**
 * Wraps all settings that can be set from the homonym menu.
 */
object Settings {

  /** Defines the value of difficulty for a game. */
  sealed trait Difficulty {
    def level: Int
  }

  class DifficultyLevel(override val level: Int) extends Difficulty

  case object Easy extends DifficultyLevel(1)
  case object Normal extends DifficultyLevel(2)
  case object Hard extends DifficultyLevel(3)

  object Time {
    case class TimeSettings(frameRate: Double = frameRate, timeRatio: Double = timeRatio)
    val frameRate: Double = 60.0
    val timeRatio: Double = 1.0
    val truncate: Double => Double = n => (n * 1000).round / 1000.toDouble
    val delay: Double => Double = n => truncate(1.0 / n)

    def elapsedTime(frameRate: Double)(implicit timeRatio: Double = 1.0): Double =
      delay(frameRate) * timeRatio
  }

  /**
   * Wrapper for all settings values. The controller should have an instance of this class and
   * should pass it to the model when is created.
   */
  case class Settings(difficulty: Difficulty = Normal, timeSettings: TimeSettings = TimeSettings())
}
