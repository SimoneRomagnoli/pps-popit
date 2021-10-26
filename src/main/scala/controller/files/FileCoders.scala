package controller.files

import alice.tuprolog.Term
import cats.effect.IO
import controller.files.FileCoders.{
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
    var objects: List[Json] = List()

    for (i <- list.indices)
      objects = objects.appended(
        Json.obj(
          ("id", Json.fromString(i.toString)),
          (
            "cells",
            Json.fromValues(
              list(i).cells.map(cell => Json.fromString("c(" + cell.x + ", " + cell.y + ")"))
            )
          )
        )
      )
    Json.obj(("images/tracks", Json.fromValues(objects)))
  }

  /**
   * Implicit decoder to convert a [[Json]] object into a list of [[Track]] s
   */
  implicit val trackDecoder: Decoder[List[Track]] = (c: HCursor) => {
    val tracks = c
      .downField("images/tracks")
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

  override def load(): Json = if (Files.exists(Paths.get(path))) {
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())
  } else {
    val empty: List[Track] = List()
    save(empty.asJson)
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())
  }

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
