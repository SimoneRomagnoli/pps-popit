package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.BaseTower

/**
 * Object that encapsulates all pre-loaded images of the game. It is useful as some view entities
 * are loaded a lot of times during a game, so it allows to earn better performances avoiding
 * repeated IO activity.
 */
object Drawings {

  sealed trait Drawable
  case object Grass extends Drawable
  case class Road(direction: String) extends Drawable
  case class Item(entity: Entity) extends Drawable

  /** Class that allows to get an image corresponding to a view entity. */
  case class Drawing(images: Drawings = Drawings()) {

    def the(drawable: Drawable): ImagePattern = drawable match {
      case Grass   => images.grass
      case Road(s) => images.road(s)
      case Item(entity) =>
        entity match {
          case balloon: Balloon =>
            balloon.life match {
              case 1 => images.redBalloon
              case 2 => images.blueBalloon
              case 3 => images.greenBalloon
            }
          case Dart()        => images.dart
          case CannonBall(_) => images.cannonBall
          case IceBall(_, _) => images.iceBall
          case BaseTower(b, _, _, _, _, _) =>
            b match {
              case Dart()        => images.arrowTower
              case CannonBall(_) => images.cannonTower
              case IceBall(_, _) => images.iceTower
            }
        }
    }
  }

  /** Class preloading all images. */
  case class Drawings(
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
      greenBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/GREEN.png")))
}
