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

import akka.actor.Actor
import javax.inject.Singleton
import play.api.Logger

@Singleton
class ReminderActor extends Actor {
  override def receive: Receive = {
    case _ => {
      Logger.info("Reminder job started")
      //TODO: Query mongo for all reminders with a next send date less than or equal to today
      //TODO: Foreach record trigger an email to be sent through digital contact
      //TODO: If response from digital contact = 202 then update reminder in mongo to have a new next send date
    }
  }
}
