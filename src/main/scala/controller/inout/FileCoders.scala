package controller.inout

import alice.tuprolog.Term
import cats.effect.IO
import controller.inout.FileCoders.{
  defaultPath,
  trackDecoder,
  trackEncoder,
  CoderBuilder,
  RichCoder
}
import io.circe._
import io.circe.syntax.EncoderOps
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm

import java.nio.charset.StandardCharsets
import java.nio.file.{ Files, Paths }
import scala.language.{ implicitConversions, postfixOps }

object FileCoders {

  val defaultPath: String = "src/main/resources/json/tracks.json"

  /**
   * Implicit encoder to convert a list of [[Track]] s into a [[Json]] object
   */
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

  /**
   * Implicit decoder to convert a [[Json]] object into a list of [[Track]] s
   */
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

  object CoderBuilder {
    var tracks: List[Track] = List()
  }

  implicit class RichCoder(io: IO[Unit]) {

    def retrieve: List[Track] = {
      io.unsafeRunSync()
      CoderBuilder.tracks
    }
  }

}

trait Coder {
  def path: String

  def save(json: Json): Unit
  def load(): Json
}

/**
 * @param path
 *   path of the file resource
 */
case class FileCoder(override val path: String = defaultPath) extends Coder {

  override def save(json: Json): Unit =
    Files.write(Paths.get(path), json.toString().getBytes(StandardCharsets.UTF_8))

  override def load(): Json =
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())

  def serialize(list: List[Track]): Unit =
    (for {
      json <- IO(list.asJson)
      _ <- IO(save(json))
    } yield ()).unsafeRunSync()

  def deserialize(): List[Track] =
    (for {
      json <- IO(load())
      tracks <- IO(json.as[List[Track]])
      _ <- IO(CoderBuilder.tracks = tracks.getOrElse(List()))
    } yield ()).retrieve

}
