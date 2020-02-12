/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.helptosavereminder.actors

import java.time.{LocalDate, LocalDateTime, LocalTime, ZoneOffset}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.helptosavereminder.models.ActorUtils._
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository
import uk.gov.hmrc.lock.{LockKeeper, LockMongoRepository, LockRepository}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class ProcessingSupervisor @Inject()(
  mongoApi: play.modules.reactivemongo.ReactiveMongoComponent,
  config: Configuration,
  httpClient: HttpClient,
  env: Environment,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext)
    extends Actor {

  lazy val repository = new HtsReminderMongoRepository(mongoApi)

  lazy val emailSenderActor: ActorRef =
    context.actorOf(
      Props(classOf[EmailSenderActor], httpClient, env, config, servicesConfig, repository, ec),
      "emailSender-actor")

  val lockrepo = LockMongoRepository(mongoApi.mongoConnector.db)

  val lockKeeper = new LockKeeper {

    override def repo: LockRepository = lockrepo //The repo created before

    override def lockId: String = "emailProcessing"

    override val forceLockReleaseAfter: org.joda.time.Duration = org.joda.time.Duration.standardMinutes(1)

    // $COVERAGE-OFF$
    override def tryLock[T](body: => Future[T])(implicit ec: ExecutionContext): Future[Option[T]] =
      repo
        .lock(lockId, serverId, forceLockReleaseAfter)
        .flatMap { acquired =>
          if (acquired) {
            body.map { case x => Some(x) }
          } else Future.successful(None)
        }
        .recoverWith { case ex => repo.releaseLock(lockId, serverId).flatMap(_ => Future.failed(ex)) }
    // $COVERAGE-ON$
  }

  val interval = 24 hours

  val startTimes: List[LocalTime] = {
    config.get[String]("scheduleFor").split(',') map {
      LocalTime.parse(_)
    }
  }.toList

  /*val uk.gov.hmrc.actors: Array[Cancellable] = startTimes map { time =>
    val delay = calculateInitialDelay(time)
    Logger.info(s"Scheduling reminder job for ${time.toString} by creating an initial delay of $delay seconds")
    system.scheduler.schedule(delay, interval, reminderActor, "")
  }*/

  private def calculateInitialDelay(time: LocalTime): FiniteDuration = {
    val now = LocalDateTime.now
    val target = LocalDateTime.of(LocalDate.now, time)

    if (target.isAfter(now)) {
      Logger.info(
        "The FUTURE schedule is " + (target.toEpochSecond(ZoneOffset.UTC) - now
          .toEpochSecond(ZoneOffset.UTC)).seconds)
      (target.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)).seconds
    } else {
      Logger.info(
        "The PRESENT schedule is " + (target.plusDays(1).toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(
          ZoneOffset.UTC)).seconds)
      (target.plusDays(1).toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)).seconds
    }
  }

  private def getNextDelayInMicros() = startTimes.map(x => calculateInitialDelay(x)).sorted.head.toSeconds

  override def receive: Receive = {

    case STOP => {
      Logger.debug("[ProcessingSupervisor] received while not processing: STOP received")
      lockrepo.releaseLock(lockKeeper.lockId, lockKeeper.serverId)
    }

    case BOOTSTRAP => {

      Logger.info("[ProcessingSupervisor] BOOTSTRAP processing started.")

      context.system.scheduler.scheduleOnce(getNextDelayInMicros() seconds, self, START)

    }

    case START => {

      lockKeeper
        .tryLock {

          repository.findHtsUsersToProcess().map {
            case Some(requests) if requests.nonEmpty => {
              Logger.info(s"[ProcessingSupervisor][receive] took ${requests.size} request/s")

              for (request <- requests) {

                emailSenderActor ! request

              }

            }
            case _ => {
              Logger.info(s"[ProcessingSupervisor][receive] no requests pending")
            }
          }

        }
        .map {
          case Some(thing) => {

            Logger.info(s"[ProcessingSupervisor][receive] OBTAINED mongo lock")

          }
          case _ => {
            Logger.info(
              s"[ProcessingSupervisor][receive] failed to OBTAIN mongo lock. Scheduling for next available slot")
            context.system.scheduler.scheduleOnce(getNextDelayInMicros() seconds, self, START)
          }

        }
      context.system.scheduler.scheduleOnce(getNextDelayInMicros() seconds, self, START)

    }
  }

}
