/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.actor._
import javax.inject.Singleton
import play.api.Logger
import uk.gov.hmrc.helptosavereminder.models.{HtsUserSchedule, HtsUserScheduleMsg, UpdateCallBackRef, UpdateCallBackSuccess}
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository

import scala.concurrent.ExecutionContext

@Singleton
class HtsUserUpdateActor(repository: HtsReminderMongoRepository)(implicit ec: ExecutionContext) extends Actor {

  override def receive: Receive = {
    case htsUserScheduleMsg: HtsUserScheduleMsg => {
      val reminder = htsUserScheduleMsg.htsUserSchedule
      repository.updateNextSendDate(reminder.nino.value, reminder.nextSendDate).map {
        case true => {
          Logger.debug(s"Updated the User nextSendDate for ${reminder.nino}")
        }
        case _ => {
          Logger.warn(s"Failed to update nextSendDate for the User: ${reminder.nino}")
        }
      }
    }

    case updateReminder: UpdateCallBackRef => {
      val origSender = sender
      repository.updateCallBackRef(updateReminder.reminder.nino.value, updateReminder.callBackRefUrl).map {
        case true => {
          Logger.debug(
            s"Updated the User callBackRef for ${updateReminder.reminder.nino.value} with value : ${updateReminder.callBackRefUrl}")
          origSender ! UpdateCallBackSuccess(
            updateReminder.reminder,
            updateReminder.callBackRefUrl,
            updateReminder.monthName)
        }
        case _ => Logger.warn(s"Failed to update CallbackRef for the User: ${updateReminder.reminder.nino.value}")
      }
    }
  }
}
