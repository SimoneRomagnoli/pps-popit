package model.spawn

import cats.effect.IO
import model.spawn.Rounds.{ Round, Streak }

import scala.language.implicitConversions

/** Providing a monadic way to define rounds. */
object RoundBuilders {

  object RoundBuilder {
    var round: Seq[Streak] = Seq()

    def empty(): Unit = round = Seq()

    def get: Round = Round(round.reverse)
  }

  /** Adds a [[Streak]] to the current [[Round]]. */
  def add(streak: Streak): IO[Unit] =
    RoundBuilder.round = streak +: RoundBuilder.round

  implicit def unitToIO(exp: => Unit): IO[Unit] = IO(exp)

  implicit class RichIO(io: IO[Unit]) {

    /** Returns the [[Round]] built. */
    def get: Round = {
      io.unsafeRunSync()
      val r: Round = RoundBuilder.get
      RoundBuilder.empty()
      r
    }
  }

}
