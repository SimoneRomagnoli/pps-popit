package model.spawn

import cats.effect.IO
import model.spawn.Rounds.{ Round, Streak }

import scala.language.implicitConversions

object RoundBuilders {

  object RoundBuilder {
    var round: Seq[Streak] = Seq()

    def empty(): Unit = round = Seq()

    def get: Round = Round(round)
  }

  def add(streak: Streak): IO[Unit] =
    RoundBuilder.round = streak +: RoundBuilder.round

  implicit def unitToIO(exp: => Unit): IO[Unit] = IO(exp)

  implicit class RichIO(io: IO[Unit]) {

    def get: Round = {
      io.unsafeRunSync()
      val r: Round = RoundBuilder.get
      RoundBuilder.empty()
      r
    }
  }

}
