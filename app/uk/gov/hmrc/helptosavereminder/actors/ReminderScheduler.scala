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

import akka.actor.{ActorRef, ActorSystem}
import javax.inject.{Inject, Named, Singleton}
import org.joda.time.DateTime
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class ReminderScheduler @Inject()(val system: ActorSystem,
                                 @Named("reminder-actor") val reminderActor: ActorRef,
                                  config: Configuration)(implicit ec: ExecutionContext) {

  val interval = config.get[FiniteDuration]("reminder-job.interval")

  // Calculate the initial delay based on the desired start hours defined in the config
  def delay: Long = {
    val now = DateTime.now()
    val start = config.get[Seq[Int]]("reminder-job.start")
    start.map( h =>
      if(now.getHourOfDay >= h) {
        now.plusDays(1)
          .withHourOfDay(h)
          .withMinuteOfHour(0)
          .withSecondOfMinute(0)
          .withMillisOfSecond(0)
          .minus(now.getMillis)
          .getMillis
      } else {
        now.withHourOfDay(h)
          .withMinuteOfHour(0)
          .withSecondOfMinute(0)
          .withMillisOfSecond(0)
          .minus(now.getMillis)
          .getMillis
      }).sorted.head
  }

  val actor = system.scheduler.schedule(delay.milliseconds, interval, reminderActor, "")
}
