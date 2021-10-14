package view.render

import javafx.scene.image.Image
import javafx.scene.paint.ImagePattern
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.{ CannonBall, Dart, IceBall }
import model.entities.towers.Towers.BaseTower
import scala.language.postfixOps

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

  /** Class that allows to get an image corresponding to a view entity. */
  case class Drawing(images: Drawings) {

    def the(drawable: Drawable): ImagePattern = images match {
      case draw: MenuDrawings =>
        drawable match {
          case Title => draw title
        }
      case draw: GameDrawings =>
        drawable match {
          case Grass   => draw grass
          case Road(s) => draw road s
          case Item(entity) =>
            entity match {
              case balloon: Balloon =>
                balloon.life match {
                  case 1 => draw redBalloon
                  case 2 => draw blueBalloon
                  case 3 => draw greenBalloon
                }
              case Dart()        => draw dart
              case CannonBall(_) => draw cannonBall
              case IceBall(_, _) => draw iceBall
              case BaseTower(b, _, _, _, _, _) =>
                b match {
                  case Dart()        => draw arrowTower
                  case CannonBall(_) => draw cannonTower
                  case IceBall(_, _) => draw iceTower
                }
            }
        }
    }
  }

  trait Drawings

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
      greenBalloon: ImagePattern = new ImagePattern(new Image("images/balloons/GREEN.png")))
      extends Drawings

  /** Class preloading all menu images. */
  case class MenuDrawings(
      title: ImagePattern = new ImagePattern(new Image("images/backgrounds/TITLE.png")))
      extends Drawings
}
