package controller.settings

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

  /**
   * Wrapper for all settings values. The controller should have an instance of this class and
   * should pass it to the model when is created.
   */
  case class Settings(difficulty: Difficulty = Hard) {
    def changeDifficulty(d: Difficulty): Settings = Settings(d)
  }
}
