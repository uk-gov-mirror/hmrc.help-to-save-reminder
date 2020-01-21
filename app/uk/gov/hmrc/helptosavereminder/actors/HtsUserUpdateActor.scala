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

import akka.actor.{Actor, ActorRef, Props}
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helptosavereminder.models.Reminder
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository

import scala.concurrent.{ExecutionContext}

@Singleton
class HtsUserUpdateActor(mongoApi: play.modules.reactivemongo.ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends Actor {

  val repository = new HtsReminderMongoRepository(mongoApi)

  override def receive: Receive = {
    case reminder: Reminder => {

      Logger.info("Updating the User " + reminder.nino)

      repository.updateNextSendDate(reminder.nino.nino).map {
        case response => {}

        case _ => //Failure
      }

      //TODO: Update reminder in mongo to have a new next send date
    }

  }
}
