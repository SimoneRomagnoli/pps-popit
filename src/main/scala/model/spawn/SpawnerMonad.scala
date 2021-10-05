package model.spawn

import cats.effect.IO
import model.spawn.SpawnManager.{ Round, Streak }

import scala.language.implicitConversions

object SpawnerMonad {

  object RoundBuilder {
    var round: Seq[Streak] = Seq()

    def get: Round = Round(round)
  }

  def build(): IO[Unit] = RoundBuilder.round = Seq()

  def add(streak: Streak): IO[Unit] =
    RoundBuilder.round = streak +: RoundBuilder.round

  implicit def unitToIO(exp: => Unit): IO[Unit] = IO(exp)

  implicit class RichIO(io: IO[Unit]) {

    def get: Round = {
      io.unsafeRunSync()
      RoundBuilder.get
    }
  }

}
