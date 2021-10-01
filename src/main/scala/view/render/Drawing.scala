package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.{ BaseTower, Tower }

object Drawing {

  sealed trait Drawable
  case object Grass extends Drawable
  case class Road(direction: String) extends Drawable
  case class Item(entity: Entity) extends Drawable

  def the(drawable: Drawable): ImagePattern = drawable match {
    case Grass   => Drawings.Backgrounds.grass
    case Road(s) => Drawings.Backgrounds.road(s)
    case Item(entity) =>
      entity match {
        case Dart()        => Drawings.Entities.Bullets.dart
        case CannonBall(_) => Drawings.Entities.Bullets.cannonBall
        case IceBall(_, _) => Drawings.Entities.Bullets.iceBall
        case BaseTower(b, _, _, _, _, _) =>
          b match {
            case Dart()        => Drawings.Entities.Towers.arrowTower
            case CannonBall(_) => Drawings.Entities.Towers.cannonTower
            case IceBall(_, _) => Drawings.Entities.Towers.iceTower
          }
      }
  }

  object Drawings {

    object Backgrounds {
      val grass: ImagePattern = new ImagePattern(new Image("images/backgrounds/GRASS.png"))

      val road: String => ImagePattern = s =>
        new ImagePattern(new Image("images/roads/" + s + ".png"))
    }

    object Entities {

      object Bullets {
        val dart: ImagePattern = new ImagePattern(new Image("images/bullets/DART.png"))
        val cannonBall: ImagePattern = new ImagePattern(new Image("images/bullets/CANNON-BALL.png"))
        val iceBall: ImagePattern = new ImagePattern(new Image("images/bullets/ICE-BALL.png"))
      }

      object Towers {
        val arrowTower: ImagePattern = new ImagePattern(new Image("images/towers/DART-TOWER.png"))

        val cannonTower: ImagePattern = new ImagePattern(
          new Image("images/towers/CANNON-BALL-TOWER.png")
        )
        val iceTower: ImagePattern = new ImagePattern(new Image("images/towers/ICE-BALL-TOWER.png"))
      }
    }
  }
}
