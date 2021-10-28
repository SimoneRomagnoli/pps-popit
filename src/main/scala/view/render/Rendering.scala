package view.render

import javafx.scene.effect.ImageInput
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.balloons.balloontypes.BalloonDecorations.BalloonDecoration
import model.entities.balloons.balloontypes.CamoBalloons.CamoBalloon
import model.entities.balloons.balloontypes.LeadBalloons.LeadBalloon
import model.entities.balloons.balloontypes.RegeneratingBalloons.RegeneratingBalloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.maps.Cells.{ Cell, GridCell }
import model.maps.Grids.Grid
import model.maps.Tracks.Directions.RIGHT
import model.maps.Tracks.Track
import scalafx.scene.effect.{ Blend, BlendMode }
import scalafx.scene.layout.Region
import scalafx.scene.paint.Color
import scalafx.scene.shape.{ Ellipse, Rectangle, Shape }
import utils.Commons.Screen.cellSize
import view.render.Drawings._
import view.render.Renders.{ renderSingle, Rendered, ToBeRendered }

import scala.annotation.tailrec
import scala.language.{ implicitConversions, reflectiveCalls }

/**
 * Object that simulates a DSL for rendering logic entities as shapes for a scalafx pane.
 */
object Rendering {
  import view.render.Rendering.RenderingUtilities._
  val drawing: Drawing = Drawing(GameDrawings())
  val defaultWidth: Double = 400.0
  val defaultHeight: Double = 200.0

  /** Renders a [[Grid]] with grass drawings. */
  def a(grid: Grid): ToBeRendered = Rendered {
    grid.cells map { cell =>
      val rect: Shape = Rendering a cell
      rect.setFill(drawing the Grass)
      rect
    }
  }

  def a(entity: Entity): ToBeRendered = an(entity)

  /** Renders an [[Entity]] with its corresponding drawing. */
  def an(entity: Entity): ToBeRendered = Rendered {
    implicit val rectangle: Rectangle = Rectangle(
      entity.position.x - entity.boundary._1 / 2,
      entity.position.y - entity.boundary._2 / 2,
      entity.boundary._1,
      entity.boundary._2
    )
    rectangle.setFill(drawing the Item(entity))
    entity match {
      case bullet: Bullet =>
        rectangle.rotate = Math.atan2(bullet.speed.y, bullet.speed.x) * 180 / Math.PI
      case tower: Tower[_] =>
        rectangle.rotate = Math.atan2(tower.direction.y, tower.direction.x) * 180 / Math.PI
        rectangle.styleClass += "tower"
      case decoration: BalloonDecoration =>
        val effects: Seq[Blend] = patternsOf(decoration).map(toBlend)
        for (i <- 0 until effects.size - 1) effects(i).bottomInput = effects(i + 1)
        //effects.sliding(2).foreach(couple => couple.head.bottomInput = couple.last)
        rectangle.setEffect(effects.head)

      case _ =>
    }
    rectangle
  }

  /** Renders the sight range of a [[Tower]]. */
  def sightOf(tower: Tower[_]): ToBeRendered = Rendered {
    val range: Ellipse =
      Ellipse(tower.position.x, tower.position.y, tower.sightRange, tower.sightRange)
    range.opacity = 0.3
    range.setFill(Color.LightGray)
    range
  }

  /** Renders a [[Cell]] just with a square [[Shape]]. */
  def a(cell: Cell): Shape =
    Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)

  /** Renders a [[Track]] as a sequence of road drawings. */
  def a(track: Track): ToBeRendered = Rendered {
    track.cells
      .prepended(GridCell(-1, 0, RIGHT))
      .sliding(2)
      .map { couple =>
        val dir: String = couple.head.direction.toString + "-" + couple.last.direction.toString
        val cell: Cell = couple.last
        val rect: Shape = Rendering a cell
        rect.setFill(drawing the Road(dir))
        rect
      }
      .toSeq
  }

  /** Renders a [[ImagePattern]] just with a square [[Shape]]. */
  def a(image: ImagePattern): ToBeRendered = Rendered {
    val rectangle: Rectangle = Rectangle(defaultWidth, defaultHeight)
    rectangle.setFill(image)
    rectangle
  }

  def setLayout(region: Region, width: Double, height: Double): Unit = {
    region.maxWidth = width
    region.minWidth = width
    region.maxHeight = height
    region.minHeight = height
  }

  private object RenderingUtilities {

    @tailrec
    def patternsOf(balloon: Balloon, patterns: Seq[BalloonPattern] = Seq()): Seq[BalloonPattern] =
      balloon match {
        case CamoBalloon(b) =>
          patternsOf(b, patterns :+ CamoPattern)
        case RegeneratingBalloon(b) =>
          patternsOf(b, patterns :+ RegeneratingPattern)
        case LeadBalloon(b) =>
          patternsOf(b, patterns :+ LeadPattern)
        case _ => patterns
      }

    def toBlend(implicit rectangle: Rectangle): BalloonPattern => Blend = { pattern =>
      val image: ImagePattern = drawing the pattern
      val blend: Blend = new Blend()
      blend.setTopInput(new ImageInput(image.getImage, rectangle.x.value, rectangle.y.value))
      pattern match {
        case CamoPattern =>
          blend.opacity = 0.6
          blend.setMode(BlendMode.Darken)
        case RegeneratingPattern =>
          blend.opacity = 0.8
          blend.setMode(BlendMode.Lighten)
        case LeadPattern =>
          blend.opacity = 1.0
          blend.setMode(BlendMode.SrcOver)
      }
      blend
    }
  }
}
