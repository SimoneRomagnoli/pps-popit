package model.managers

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import controller.Controller.ControllerMessages.{ BoostTowerIn, TowerOption }
import controller.GameLoop.GameLoopMessages.ModelUpdated
import controller.Messages.{ EntitiesManagerMessage, Input, Update, WithReplyTo }
import model.Model.ModelMessages.{ Lose, Pay }
import model.actors.BalloonMessages.{ BalloonKilled, Hit }
import model.actors.BulletMessages.{ BalloonHit, BulletKilled, StartExplosion }
import model.actors.{ BalloonActor, BulletActor, TowerActor }
import model.actors.TowerMessages.Boost
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.Balloon
import model.entities.bullets.Bullets.Bullet
import model.entities.towers.Towers.Tower
import model.managers.EntitiesMessages.{
  EntitySpawned,
  EntityUpdated,
  ExitedBalloon,
  SpawnEntity,
  TickUpdate,
  TowerIn,
  UpdateEntity
}
import model.maps.Cells.Cell

case class EntityActor(actorRef: ActorRef[Update], entity: Entity)

object EntitiesMessages {

  case class UpdateEntity(elapsedTime: Double, entities: List[Entity], replyTo: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage

  case class EntityUpdated(entity: Entity, ref: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage
  case class SpawnEntity(entity: Entity) extends Update with EntitiesManagerMessage

  case class EntitySpawned(entity: Entity, actor: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage

  case class ExitedBalloon(balloon: Balloon, actorRef: ActorRef[Update])
      extends Update
      with EntitiesManagerMessage
  case class TowerIn(cell: Cell) extends Update with EntitiesManagerMessage

  case class TickUpdate(elapsedTime: Double, replyTo: ActorRef[Input])
      extends Update
      with EntitiesManagerMessage
}

object EntitiesManager {

  def apply(model: ActorRef[Update]): Behavior[Update] = Behaviors.setup { ctx =>
    EntityManager(ctx, model).running()
  }
}

case class EntityManager private (
    ctx: ActorContext[Update],
    model: ActorRef[Update],
    var entities: List[EntityActor] = List(),
    var messageQueue: Seq[Update] = Seq()) {

  def dequeue(): Behavior[Update] = {
    messageQueue.foreach(ctx.self ! _)
    messageQueue = Seq()
    running()
  }

  def running(): Behavior[Update] = Behaviors.receiveMessage {
    case TickUpdate(elapsedTime, replyTo) =>
      entities.map(_.actorRef).foreach {
        _ ! UpdateEntity(elapsedTime, entities.map(_.entity), ctx.self)
      }
      updating(replyTo)

    case EntityUpdated(a, b) =>
      println(a + b.toString)
      Behaviors.same

    case SpawnEntity(entity) =>
      ctx.self ! EntitySpawned(entity, entitySpawned(entity, ctx))
      Behaviors.same

    case EntitySpawned(entity, actor) =>
      entities = EntityActor(actor, entity) :: entities
      Behaviors.same

    case WithReplyTo(msg, replyTo) =>
      msg match {
        case TowerIn(cell) =>
          val tower: Option[Tower[Bullet]] = entities
            .map(_.entity)
            .collectFirst {
              case tower: Tower[Bullet] if tower.position == cell.centralPosition => tower
            }
          replyTo ! TowerOption(tower)

        case BoostTowerIn(cell, powerUp) =>
          model ! Pay(powerUp.cost)
          entities.collect {
            case EntityActor(actorRef, entity) if cell.contains(entity.position) =>
              actorRef
          }.head ! Boost(powerUp, replyTo)
      }
      Behaviors.same

    case BalloonKilled(actorRef) =>
      entities = entities.filter(_.actorRef != actorRef)
      Behaviors.same

    case _ => Behaviors.same
  }

  def updating(
      replyTo: ActorRef[Input],
      updatedEntities: List[EntityActor] = List(),
      animations: List[Entity] = List()): Behavior[Update] = Behaviors.receiveMessage {
    case EntityUpdated(entity, ref) =>
      EntityActor(ref, entity) :: updatedEntities match {
        case full if full.size == entities.size =>
          val (balloons, others): (List[Entity], List[Entity]) =
            full.map(_.entity).partition(_.isInstanceOf[Balloon])
          replyTo ! ModelUpdated(
            others.appendedAll(balloons.asInstanceOf[List[Balloon]].sorted),
            animations
          )
          entities = full
          dequeue()
        case notFull => updating(replyTo, notFull, animations)
      }

    case ExitedBalloon(balloon, actorRef) =>
      model ! Lose(balloon.life)
      ctx.self ! BalloonKilled(actorRef)
      Behaviors.same

    case BulletKilled(actorRef) =>
      killEntity(updatedEntities, replyTo, actorRef, animations)

    case BalloonKilled(actorRef) =>
      if (updatedEntities.map(_.actorRef).contains(actorRef)) {
        entities = entities.filter(_.actorRef != actorRef)
        updating(replyTo, updatedEntities.filter(_.actorRef != actorRef), animations)
      } else {
        killEntity(updatedEntities, replyTo, actorRef, animations)
      }

    case BalloonHit(bullet, balloons) =>
      entities.filter(e => balloons.contains(e.entity)).foreach {
        _.actorRef ! Hit(bullet, ctx.self)
      }
      Behaviors.same

    case StartExplosion(bullet) =>
      updating(replyTo, updatedEntities, bullet :: animations)

    case EntitySpawned(entity, actor) =>
      entities = EntityActor(actor, entity) :: entities
      ctx.self ! EntityUpdated(entity, actor)
      updating(replyTo, updatedEntities, animations)

    case msg =>
      if (!msg.isInstanceOf[TickUpdate]) {
        println(messageQueue)
        messageQueue = messageQueue :+ msg
      }
      Behaviors.same
  }

  def killEntity(
      updatedEntities: List[EntityActor],
      replyTo: ActorRef[Input],
      actorRef: ActorRef[Update],
      animations: List[Entity]): Behavior[Update] = updatedEntities match {
    case full if full.size == entities.size - 1 =>
      replyTo ! ModelUpdated(full.map(_.entity), animations)
      entities = full
      dequeue()
    case notFull =>
      entities = entities.filter(_.actorRef != actorRef)
      updating(replyTo, notFull, animations)
  }

  def entitySpawned(entity: Entity, ctx: ActorContext[Update]): ActorRef[Update] = entity match {
    case balloon: Balloon => ctx.spawnAnonymous(BalloonActor(balloon))
    case tower: Tower[_]  => ctx.spawnAnonymous(TowerActor(tower))
    case bullet: Bullet   => ctx.spawnAnonymous(BulletActor(bullet))
  }
}
