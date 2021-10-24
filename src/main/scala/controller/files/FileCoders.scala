package controller.files

import alice.tuprolog.Term
import cats.effect.IO
import controller.files.FileCoders.{ defaultPath, here }
import io.circe._
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }
import scala.language.implicitConversions

object FileCoders {

  object here

  val defaultPath: String = "src/main/resources/json/tracks.json"

  implicit val trackEncoder: Encoder[List[Track]] = (list: List[Track]) => {
    val iterator = list.iterator
    var objects: List[Json] = List()

    while (iterator.hasNext) {
      val track: Track = iterator.next()
      objects = objects.appended(
        Json.obj(
          ("id", Json.fromString(list.indexOf(track).toString)),
          (
            "cells",
            Json.fromValues(
              track.cells.map(cell => Json.fromString("c(" + cell.x + ", " + cell.y + ")"))
            )
          )
        )
      )
    }
    Json.obj(("tracks", Json.fromValues(objects)))
  }

  implicit val trackDecoder: Decoder[List[Track]] = (c: HCursor) => {
    val tracks = c
      .downField("tracks")
      .focus
      .flatMap(_.asArray)
      .getOrElse(Vector.empty)
      .flatMap(_.hcursor.downField("cells").as[List[String]].toOption)
      .map(s => Term.createTerm(s.mkString("[", ", ", "]")))
      .map(trackFromTerm)
      .toList
      .map(Track(_))

    Right(tracks)
  }

  implicit def unitToIO(exp: => Unit): IO[Unit] = IO(exp)

  implicit class RichIO(io: IO[Unit]) {

    def get: List[Track] = {
      io.unsafeRunSync()
      List()
    }
  }

  object Builder {}

}

trait Coder {
  def path: String

  def save(json: Json): Unit
  def load(o: here.type): Json
}

case class FileCoder(override val path: String = defaultPath) extends Coder {

  override def save(json: Json): Unit =
    Files.write(Paths.get(path), json.toString().getBytes(StandardCharsets.UTF_8))

  override def load(o: here.type): Json =
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())

}
