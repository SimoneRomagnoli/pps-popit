package model

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages._
import controller.GameLoop.GameLoopMessages.{ MapCreated, ModelUpdated }
import controller.Messages._
import model.Model.ModelMessages._
import model.actors.{ BalloonActor, BulletActor, TowerActor }
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.TickUpdate
import model.managers.{ EntitiesManager, EntityActor, SpawnManager }
import model.maps.Tracks.Track
import model.stats.Stats.GameStats
import utils.Constants.Maps.gameGrid

import scala.language.postfixOps

/**
 * Model of the application, fundamental in the MVC pattern. It receives [[Update]] messages from
 * the game loop and updates the actors governing game entities.
 */
object Model {

  object ModelMessages {
    case class NewMap(replyTo: ActorRef[Input]) extends Update
    case class WalletQuantity(replyTo: ActorRef[Input]) extends Update
    case class Pay(amount: Int) extends Update
    case class Lose(amount: Int) extends Update
  }

  object ModelActor {

    def apply(controller: ActorRef[Input]): Behavior[Update] = Behaviors setup { ctx =>
      ModelActor(ctx, controller).init()
    }
  }

  /**
   * The model actor has three behaviors:
   *   - init, in which it just starts a new game by creating a map and a spawner;
   *   - running, in which it waits for a [[TickUpdate]] from the game loop to update the game
   *     entities;
   *   - updating, in which waits for entities to be updated and then notifies the game loop about
   *     it.
   */
  case class ModelActor private (
      ctx: ActorContext[Update],
      controller: ActorRef[Input],
      stats: GameStats = GameStats(),
      var entities: List[EntityActor] = List(),
      var track: Track = Track(),
      var handlers: List[(ActorRef[Update], MessageType)] = List()) {

    def init(): Behavior[Update] = Behaviors.receiveMessage { case NewMap(replyTo) =>
      track = Track(gameGrid)
      handlers = (ctx.spawnAnonymous(SpawnManager(ctx.self, track)), SpawnMessage) :: handlers
      handlers = (ctx.spawnAnonymous(EntitiesManager(ctx.self)), EntityMessage) :: handlers

      replyTo ! MapCreated(track)
      running()
    }

    def running(): Behavior[Update] =
      Behaviors.receiveMessage {
        /*case StartNextRound() =>
          handle(StartNextRound())
          Behaviors.same

        case SpawnEntity(entity) =>
          ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
          Behaviors.same

        case EntitySpawned(entity, actor) =>
          entities = EntityActor(actor, entity) :: entities
          Behaviors.same

        case TowerIn(cell) =>
          val tower: Option[Tower[Bullet]] = entities
            .map(_.entity)
            .collectFirst {
              case tower: Tower[Bullet] if tower.position == cell.centralPosition => tower
            }
          controller ! TowerOption(tower)
          Behaviors.same

        case WithReplyTo(message, replyTo) =>
          handle(WithReplyTo(message, replyTo))
          Behaviors.same*/

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        /*case BoostTowerIn(cell, powerUp) =>
          entities.collect {
            case EntityActor(actorRef, entity) if cell.contains(entity.position) =>
              actorRef
          }.head ! Boost(powerUp, ctx.self)
          Behaviors.same

        case TowerBoosted(tower, actorRef) =>
          controller ! TowerBoosted(tower, actorRef)
          entities = entities.filter(_.actorRef != actorRef).appended(EntityActor(actorRef, tower))
          Behaviors.same

        case BalloonKilled(actorRef) =>
          entities = entities.filter(_.actorRef != actorRef)
          Behaviors.same*/

        case msg =>
          handle(msg)
          Behaviors.same
      }

    def updating(
        replyTo: ActorRef[Input],
        updatedEntities: List[EntityActor] = List()): Behavior[Update] =
      Behaviors.receiveMessage {
        /*case EntityUpdated(entity, ref) =>
          EntityActor(ref, entity) :: updatedEntities match {
            case full if full.size == entities.size =>
              val (balloons, others): (List[Entity], List[Entity]) =
                full.map(_.entity).partition(_.isInstanceOf[Balloon])
              replyTo ! ModelUpdated(
                others.appendedAll(balloons.asInstanceOf[List[Balloon]].sorted)
              )
              entities = full
              running()
            case notFull => updating(replyTo, notFull)
          }

        case StartNextRound() =>
          handle(StartNextRound())
          Behaviors.same

        case SpawnEntity(entity) =>
          ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
          Behaviors.same

        case EntitySpawned(entity, actor) =>
          entities = EntityActor(actor, entity) :: entities
          ctx.self ! EntityUpdated(entity, actor)
          updating(replyTo, updatedEntities)

        case ExitedBalloon(balloon, actorRef) =>
          stats lose balloon.life
          ctx.self ! BalloonKilled(actorRef)
          Behaviors.same

        case BoostTowerIn(cell, powerUp) =>
          stats spend powerUp.cost
          entities.collect {
            case EntityActor(actorRef, entity) if cell.contains(entity.position) =>
              actorRef
          }.head ! Boost(powerUp, ctx.self)
          Behaviors.same

        case TowerBoosted(tower, actorRef) =>
          controller ! TowerBoosted(tower, actorRef)
          entities = entities.filter(_.actorRef != actorRef).appended(EntityActor(actorRef, tower))
          Behaviors.same

        case BulletKilled(actorRef) =>
          killEntity(updatedEntities, replyTo, actorRef)

        case BalloonKilled(actorRef) =>
          if (updatedEntities.map(_.actorRef).contains(actorRef)) {
            entities = entities.filter(_.actorRef != actorRef)
            updating(replyTo, updatedEntities.filter(_.actorRef != actorRef))
          } else {
            killEntity(updatedEntities, replyTo, actorRef)
          }*/

        case WalletQuantity(replyTo) =>
          replyTo ! CurrentWallet(stats.wallet)
          Behaviors.same

        case Pay(amount) =>
          stats spend amount
          Behaviors.same

        /*case BalloonHit(bullet, balloons) =>
          entities.filter(e => balloons.contains(e.entity)).foreach {
            _.actorRef ! Hit(bullet, ctx.self)
          }
          Behaviors.same

        case StartExplosion(bullet) =>
          controller ! StartAnimation(bullet)
          Behaviors.same*/

        case msg =>
          handle(msg)
          Behaviors.same
      }

    def killEntity(
        updatedEntities: List[EntityActor],
        replyTo: ActorRef[Input],
        actorRef: ActorRef[Update]): Behavior[Update] = updatedEntities match {
      case full if full.size == entities.size - 1 =>
        replyTo ! ModelUpdated(full.map(_.entity))
        entities = full
        running()
      case notFull =>
        entities = entities.filter(_.actorRef != actorRef)
        updating(replyTo, notFull)
    }

    def handle(msg: Update): Unit = msg match {
      case WithReplyTo(m, _) => choose(messageType(m)).foreach(_ ! msg)
      case msg               => choose(messageType(msg)).foreach(_ ! msg)
      case _                 =>
    }

    def choose(messageType: MessageType)(implicit
        handlers: List[(ActorRef[Update], MessageType)] = handlers): List[ActorRef[Update]] =
      handlers.collect {
        case (actorRef, msgType) if msgType == messageType => actorRef
      }
  }

  def entitySpawned(entity: Entity, ctx: ActorContext[Update]): ActorRef[Update] = entity match {
    case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
    case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
    case bullet: Bullet   => ctx.spawnAnonymous(BulletActor(bullet))
  }

}
