package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Simple
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.BaseTower

object Drawings {

  sealed trait Drawable
  case object Grass extends Drawable
  case class Road(direction: String) extends Drawable
  case class Item(entity: Entity) extends Drawable

  case class Drawing(images: Drawings = Drawings()) {

    def the(drawable: Drawable): ImagePattern = drawable match {
      case Grass   => images.grass
      case Road(s) => images.road(s)
      case Item(entity) =>
        entity match {
          case Simple(_, _, _, _) => images.redBalloon
          case Dart()             => images.dart
          case CannonBall(_)      => images.cannonBall
          case IceBall(_, _)      => images.iceBall
          case BaseTower(b, _, _, _, _, _) =>
            b match {
              case Dart()        => images.arrowTower
              case CannonBall(_) => images.cannonTower
              case IceBall(_, _) => images.iceTower
            }
        }
    }

  }

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
      redBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/RED.png")))
}
