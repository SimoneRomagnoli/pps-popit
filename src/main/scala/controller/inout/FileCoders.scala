package controller.inout

import alice.tuprolog.Term
import cats.effect.IO
import controller.inout.FileCoders.CoderBuilder.{ appDir, jsonPath }
import controller.inout.FileCoders.{ trackDecoder, trackEncoder, CoderBuilder, RichCoder }
import io.circe._
import io.circe.syntax.EncoderOps
import model.maps.Tracks.Track
import model.maps.prolog.PrologUtils.Solutions.trackFromTerm

import java.nio.charset.StandardCharsets
import java.nio.file
import java.nio.file.{ Files, Paths }
import scala.language.{ implicitConversions, postfixOps }
import scala.reflect.io.Path

object FileCoders {

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

  /**
   * Builder to setup I/O path destinations
   */
  object CoderBuilder {

    val userHome: String = System.getProperty("user.home")
    val separator: String = System.getProperty("file.separator")

    val appDir: String = userHome + separator + ".popit"

    val filesDir: String = appDir + separator + "files" + separator + "json"
    val jsonPath: String = filesDir + separator + "tracks.json"
    val imagesDir: String = appDir + separator + "images" + separator + "tracks"

    def trackURL(index: Int): String = "file:///" + imagesDir + separator + "track" + index + ".png"

    var tracks: List[Track] = List()

    /**
     * Check if resource directories already exists, and if they not, create them
     */
    implicit class FileMonad(path: String) {
      def check: Option[String] = if (Files.notExists(Paths.get(path))) Some(path) else None

      def create: Option[file.Path] =
        if (check.isDefined) Some(Files.createDirectories(Paths.get(check.get))) else None
    }

    def setup(): Unit = for {
      checkFiles <- filesDir.check
      checkImages <- imagesDir.check
      _ <- checkFiles.create
      _ <- checkImages.create
    } yield ()

  }

  /**
   * Implicit coder to retrieve the result after a sequence of I/O operations
   * @param io
   *   the I/O operation
   */
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
  def clean(): Unit
}

/**
 * Customized file coder to handle I/O on local json file. Permits to serialize, deserialize, save
 * and load the json file containing the list of saved [[Track]] s
 * @param path
 *   path of the file resource
 */
case class FileCoder(override val path: String = jsonPath) extends Coder {

  CoderBuilder.setup()

  override def save(json: Json): Unit =
    Files.write(Paths.get(path), json.toString().getBytes(StandardCharsets.UTF_8))

  override def load(): Json = if (Files.exists(Paths.get(path))) {
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())
  } else {
    val empty: List[Track] = List()
    save(empty.asJson)
    parser.parse(Files.readString(Paths.get(path), StandardCharsets.UTF_8)).getOrElse(Json.obj())
  }

  override def clean(): Unit = {
    Path(appDir).deleteRecursively()
    CoderBuilder.setup()
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
