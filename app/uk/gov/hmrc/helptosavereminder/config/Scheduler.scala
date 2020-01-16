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

package uk.gov.hmrc.helptosavereminder.config

import akka.actor.{ActorRef, ActorSystem, Props}
import javax.inject.{Inject, Named, Singleton}
import play.api.inject.DefaultApplicationLifecycle
import play.api.{Application, Configuration, Environment}
import uk.gov.hmrc.helptosavereminder.actors.ReminderScheduler

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Scheduler @Inject() (
  actorSystem: ActorSystem,
  env: Environment,
  mongoApi: play.modules.reactivemongo.ReactiveMongoComponent,
  config: Configuration
)(implicit val ec: ExecutionContext) {

  lazy val reminderSupervisor = actorSystem.actorOf(
    Props(classOf[ReminderScheduler], actorSystem, mongoApi, config, ec),
    "reminder-supervisor"
  )

  reminderSupervisor ! "START"

}
