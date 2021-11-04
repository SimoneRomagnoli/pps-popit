package controller

import akka.actor.testkit.typed.scaladsl.{ ScalaTestWithActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.AskPattern.{ schedulerFromActorSystem, Askable }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import commons.Futures.retrieve
import controller.Controller.ControllerActor
import controller.Controller.ControllerMessages.{ NewTrack, StartNextRound }
import controller.PerformancesTest.PerformanceMessages._
import controller.PerformancesTest._
import controller.interaction.GameLoop.GameLoopMessages._
import controller.interaction.Messages.{ Input, Render, Update }
import controller.settings.Settings.Settings
import controller.settings.Settings.Time.{ delay, elapsedTime, TimeSettings }
import model.Model.ModelMessages.TickUpdate
import model.actors.BalloonActor
import model.entities.Entities.Entity
import model.entities.balloons.Balloons.{ simple, Balloon }
import model.managers.EntitiesMessages.{ EntityUpdated, ExitedBalloon, UpdateEntity }
import model.maps.Tracks.Track
import org.scalatest.Ignore
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationDouble

object PerformancesTest {

  object PerformanceMessages {
    case object PerformanceTick extends Input

    case class Performance(singleUpdateTime: Double) extends Input
    case class Performances(replyTo: ActorRef[Render]) extends Input
    case class StopPerformances() extends Input

    case class AveragePerformances(updateAverageTime: Double, updateFrequency: Double)
        extends Render
  }

  /** Dummy model waiting for tick message from game loop. */
  val runningModel: (List[Entity], List[ActorRef[Update]], ActorRef[Input]) => Behavior[Update] =
    (entities, actors, stopper) =>
      Behaviors.setup { ctx =>
        Behaviors.receiveMessagePartial {
          case TickUpdate(elapsedTime, replyTo) =>
            actors foreach {
              _ ! UpdateEntity(elapsedTime, entities, ctx.self)
            }
            updatingModel(entities, actors, replyTo, stopper)
          case _ => Behaviors.same
        }
      }

  /** Dummy model waiting for all entities to be updated. */
  val updatingModel: (
      List[Entity],
      List[ActorRef[Update]],
      ActorRef[Input],
      ActorRef[Input]) => Behavior[Update] =
    (entities, actors, gameLoop, stopper) => {
      var updatedEntities: List[Entity] = List()
      Behaviors.receiveMessagePartial {
        case EntityUpdated(entity, _) =>
          updatedEntities = entity :: updatedEntities
          updatedEntities match {
            case _ if updatedEntities.size == entities.size =>
              gameLoop ! ModelUpdated(updatedEntities, List())
              runningModel(updatedEntities, actors, stopper)
            case _ => Behaviors.same
          }

        case ExitedBalloon(_, _) =>
          stopper ! StopPerformances()
          Behaviors.stopped

        case _ => Behaviors.same
      }
    }

  /**
   * Dummy game loop that simply ticks the model and takes the time.
   */
  object PerformancesGameLoop {

    def apply(
        model: ActorRef[Update],
        performancesWatcher: ActorRef[Input],
        timeSettings: TimeSettings): Behavior[Input] = Behaviors.setup { ctx =>
      Behaviors.receiveMessagePartial { case Start() =>
        Behaviors.withTimers { timers =>
          timers.startTimerWithFixedDelay(Tick, delay(timeSettings.frameRate).seconds)
          PerformancesGameLoop(ctx, model, performancesWatcher, timeSettings).running()
        }
      }
    }
  }

  case class PerformancesGameLoop private (
      ctx: ActorContext[Input],
      model: ActorRef[Update],
      performancesWatcher: ActorRef[Input],
      timeSettings: TimeSettings,
      var ticks: Int = 0) {
    var tickStarts: List[Long] = List()
    var updates: Int = 0

    private def running(): Behavior[Input] = Behaviors.receiveMessagePartial {
      case Tick =>
        val tickStart: Long = System.currentTimeMillis()
        tickStarts = tickStarts appended tickStart
        ticks += 1
        val elapsed: Double = elapsedTime(timeSettings.frameRate)(timeSettings.timeRatio)
        model ! TickUpdate(elapsed, ctx.self)
        Behaviors.same

      case ModelUpdated(_, _) =>
        val finish: Long = System.currentTimeMillis()
        val updateTime: Double = TimeUnit.MILLISECONDS.toMillis(finish - tickStarts(updates))
        updates += 1
        performancesWatcher ! Performance(updateTime)
        Behaviors.same

      case CanStartNextRound() =>
        performancesWatcher ! StopPerformances()
        Behaviors.same
    }
  }

  /**
   * Simple actor that simply elaborates some performances statistics from those received by the
   * game loop.
   */
  case class PerformancesWatcher private (
      ctx: ActorContext[Input],
      performancesStopper: ActorRef[Input],
      var renders: Int = 0,
      var updateTime: Double = 0.0,
      var updateAverageTime: Double = 0.0,
      var lastRender: Long = 0L,
      var timeAmongRenders: Double = 0.0) {

    def running(): Behavior[Input] = Behaviors.receiveMessagePartial {
      case Performance(singleUpdateTime) =>
        val currentRenderTime: Long = System.nanoTime()
        if (renders > 0) {
          val elapsedSinceLastRender: Double =
            TimeUnit.NANOSECONDS.toMillis(currentRenderTime - lastRender)
          timeAmongRenders += elapsedSinceLastRender
        }
        lastRender = currentRenderTime
        renders += 1
        updateTime += singleUpdateTime
        updateAverageTime = updateTime / renders
        Behaviors.same

      case Performances(replyTo) =>
        replyTo ! AveragePerformances(updateAverageTime, timeAmongRenders / (renders.toDouble + 1))
        Behaviors.stopped

      case StopPerformances() =>
        performancesStopper ! StopPerformances()
        Behaviors.same
    }
  }
}

@Ignore
class PerformancesTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val view: TestProbe[Render] = testKit.createTestProbe[Render]()
  val trackLoader: TestProbe[Input] = testKit.createTestProbe[Input]()
  val performancesStopper: TestProbe[Input] = testKit.createTestProbe[Input]()
  val track: Track = Track()

  val numberOfActors: Int = 1000

  val entities: List[Balloon] =
    LazyList.continually(simple() on track in track.start).take(numberOfActors).toList

  val actors: List[ActorRef[Update]] = entities.map(b => testKit spawn BalloonActor(b))

  val model: ActorRef[Update] =
    testKit spawn runningModel(entities, actors, performancesStopper.ref)

  val performancesWatcher: ActorRef[Input] = testKit spawn {
    Behaviors.setup { ctx: ActorContext[Input] =>
      PerformancesWatcher(ctx, performancesStopper.ref).running()
    }
  }

  val gameLoop: ActorRef[Input] = testKit spawn {
    PerformancesGameLoop(model, performancesWatcher, TimeSettings(timeRatio = 5.0))
  }

  val controller: ActorRef[Input] = testKit spawn {
    Behaviors.setup { ctx: ActorContext[Input] =>
      ControllerActor(ctx, view.ref, trackLoader.ref, Settings(), Some(model), Some(gameLoop))
        .default()
    }
  }

  "The Performances" when {
    "a normal game is on" should {
      "not be slow" in {
        controller ! NewTrack()
        gameLoop ! Start()
        controller ! StartNextRound()

        performancesStopper.within(100.seconds) {
          performancesStopper expectMessage StopPerformances()
          retrieve(performancesWatcher ? Performances) {
            case AveragePerformances(updateAverageTime, renderAverageTime) =>
              println("Render Average Time: " + renderAverageTime + " milliseconds.")
              println("Update Average Time: " + updateAverageTime + " milliseconds.")
            case _ =>
          }
        }
      }
    }
  }

}
