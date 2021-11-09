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
import model.maps.Tracks.Directions.Right
import model.maps.Tracks.Track
import scalafx.scene.effect.{ Blend, BlendMode }
import scalafx.scene.layout.Region
import scalafx.scene.paint.Color
import scalafx.scene.shape.{ Ellipse, Rectangle, Shape }
import commons.CommonValues.Screen.cellSize
import view.render.Drawings._
import view.render.Rendering.Drawers.Drawer
import view.render.Renders.{ renderSingle, Renderable, Rendered }

import scala.annotation.tailrec
import scala.language.{ implicitConversions, reflectiveCalls }

/**
 * Object that simulates a DSL for rendering logic entities as shapes for a scalafx pane.
 */
object Rendering {
  import view.render.Rendering.RenderingPatterns._
  val drawing: Drawing = Drawing(GameDrawings())

  /** Sets the layout of the specified region with the specified width and height. */
  def setLayout(region: Region, width: Double, height: Double): Unit = {
    region.maxWidth = width
    region.minWidth = width
    region.maxHeight = height
    region.minHeight = height
  }

  /**
   * Draws a generic element in a [[Shape]] and gives the possibility to put it in a ScalaFX node.
   *
   * @param element,
   *   the generic element to be drawn
   * @param drawer,
   *   the implicit [[Drawer]] that defines how the drawing happens
   * @tparam T,
   *   the type of the drawn element
   * @return
   *   a [[Renderable]] element
   */
  def a[T](element: T)(implicit drawer: Drawer[T]): Renderable = drawer.draw(element)

  /** Contains all the implicit drawers that allow to render graphical elements. */
  object Drawers {

    /**
     * Allows to turn a generic element into a [[Renderable]]. It is necessarily contravariant
     * because of the entities' hierarchy: an entities drawer should be able to draw any [[Entity]]
     * sub-type.
     *
     * @tparam T,
     *   the type of the element to be drawn.
     */
    trait Drawer[-T] {
      def draw(elem: T): Renderable
    }

    /** Renders a [[Grid]] with grass drawings. */
    implicit val gridDrawer: Drawer[Grid] = (grid: Grid) =>
      Rendered {
        grid.cells map { cell =>
          val rect: Shape = (Rendering a cell).asSingle
          rect.setFill(drawing the Grass)
          rect
        }
      }

    /** Renders a [[Cell]] just with a square [[Shape]]. */
    implicit val cellDrawer: Drawer[Cell] = (cell: Cell) =>
      Rendered {
        Rectangle(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize)
      }

    /** Renders a [[Track]] as a sequence of road drawings. */
    implicit val trackDrawer: Drawer[Track] = (track: Track) =>
      Rendered {
        track.cells
          .prepended(GridCell(-1, 0, Right))
          .sliding(2)
          .map { couple =>
            val dir: String = couple.head.direction.toString + "-" + couple.last.direction.toString
            val cell: Cell = couple.last
            val rect: Shape = (Rendering a cell).asSingle
            rect.setFill(drawing the Road(dir))
            rect
          }
          .toSeq
      }

    /** Renders the sight range of a [[Tower]]. */
    implicit val towerSightDrawer: Drawer[(Double, Double, Double)] =
      (t: (Double, Double, Double)) =>
        Rendered {
          val range: Ellipse = Ellipse(t._1, t._2, t._3, t._3)
          range.opacity = 0.3
          range.setFill(Color.LightGray)
          range
        }

    /** Renders an [[Entity]] with its corresponding drawing. */
    implicit val entityDrawer: Drawer[Entity] = (entity: Entity) =>
      Rendered {
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
            rectangle.setEffect(effects.head)

          case _ =>
        }
        rectangle
      }

    /** Renders a [[ImagePattern]] just with a square [[Shape]]. */
    implicit val imagePatternDrawer: Drawer[ImagePattern] = (image: ImagePattern) =>
      Rendered {
        val rectangle: Rectangle = Rectangle(image.getImage.getWidth, image.getImage.getHeight)
        rectangle.setFill(image)
        rectangle
      }
  }

  /** Contains methods for blending balloon patterns. */
  private object RenderingPatterns {

    /**
     * Given a [[Balloon]], returns a sequence of [[BalloonPattern]] that represent the balloon
     * types that it inherits.
     *
     * @param balloon,
     *   the balloon instance.
     * @param patterns,
     *   an accumulator for the patterns allowing tail recursion.
     * @return
     *   the sequence of [[BalloonPattern]] inherited by the balloon.
     */
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

    /**
     * Transforms a [[BalloonPattern]] into a [[Blend]] effect. It needs a [[Rectangle]] to resize
     * the effect at its size; it is set as implicit so the method can be used in a more functional
     * way.
     *
     * @param rectangle,
     *   the shape that represents the size of the effect.
     * @return
     *   the blend effect of the balloon pattern.
     */
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
