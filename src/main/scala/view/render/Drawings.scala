package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.BaseTower
import utils.Constants.Entities.Balloons.balloonDefaultBoundary

/**
 * Object that encapsulates all pre-loaded images of the game. It is useful as some view entities
 * are loaded a lot of times during a game, so it allows to earn better performances avoiding
 * repeated IO activity.
 */
object Drawings {

  sealed trait Drawable
  case object Title extends Drawable
  case object Grass extends Drawable
  case class Road(direction: String) extends Drawable
  case class Item(entity: Entity) extends Drawable

  sealed trait BalloonPattern extends Drawable
  case object CamoPattern extends BalloonPattern
  case object LeadPattern extends BalloonPattern
  case object RegeneratingPattern extends BalloonPattern

  /** Class that allows to get an image corresponding to a view entity. */
  case class Drawing(images: Drawings) {

    def the(drawable: Drawable): ImagePattern = images match {
      case drawing: MenuDrawings =>
        drawable match {
          case Title => drawing.title
          case _     => null
        }
      case drawing: GameDrawings =>
        drawable match {
          case Grass   => drawing.grass
          case Road(s) => drawing.road(s)
          case pattern: BalloonPattern =>
            pattern match {
              case CamoPattern         => drawing.camoBalloon
              case RegeneratingPattern => drawing.regeneratingBalloon
            }
          case Item(entity) =>
            entity match {
              case balloon: Balloon =>
                balloon.life match {
                  case 1 => drawing.redBalloon
                  case 2 => drawing.blueBalloon
                  case 3 => drawing.greenBalloon
                }
              case Dart()        => drawing.dart
              case CannonBall(_) => drawing.cannonBall
              case IceBall(_, _) => drawing.iceBall
              case BaseTower(b, _, _, _, _, _, _) =>
                b match {
                  case Dart()        => drawing.arrowTower
                  case CannonBall(_) => drawing.cannonTower
                  case IceBall(_, _) => drawing.iceTower
                }
            }
          case _ => null
        }
    }
  }

  trait Drawings

  val (x: Double, y: Double) = balloonDefaultBoundary

  /** Class preloading all game images. */
  case class GameDrawings(
      grass: ImagePattern = new ImagePattern(new Image("images/backgrounds/GRASS.png")),
      road: String => ImagePattern = s => new ImagePattern(new Image("images/roads/" + s + ".png")),
      dart: ImagePattern = new ImagePattern(new Image("images/bullets/DART.png")),
      cannonBall: ImagePattern = new ImagePattern(new Image("images/bullets/CANNON-BALL.png")),
      iceBall: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-BALL.png")),
      arrowTower: ImagePattern = new ImagePattern(new Image("images/towers/DART-TOWER.png")),
      cannonTower: ImagePattern = new ImagePattern(
        new Image("images/towers/CANNON-BALL-TOWER.png")
      ),
      iceTower: ImagePattern = new ImagePattern(new Image("images/towers/ICE-BALL-TOWER.png")),
      redBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/RED.png")),
      blueBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/BLUE.png")),
      greenBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/GREEN.png")),
      camoBalloon: ImagePattern = new ImagePattern(
        new Image("images/balloons/CAMO.png", x, y, false, false)
      ),
      regeneratingBalloon: ImagePattern = new ImagePattern(
        new Image("images/balloons/REGENERATING.png", x, y, false, false)
      ))
      extends Drawings

  /** Class preloading all menu images. */
  case class MenuDrawings(
      title: ImagePattern = new ImagePattern(new Image("images/backgrounds/TITLE.png")))
      extends Drawings
}
