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

package uk.gov.hmrc.helptosavereminder.models

import java.time.LocalDate

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

case class Reminder(
  nino: Nino,
  email: Email,
  name: String,
  optInStatus: Boolean,
  daysToReceive: Seq[Int],
  nextSendDate: LocalDate,
  callBackUrlRef: String)

object Reminder {
  implicit val emailFormat = Json.format[Email]
  implicit val dateFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val idFormat = ReactiveMongoFormats.objectIdFormats
  implicit val reminderFormat: Format[Reminder] = Json.format[Reminder]
}

object ActorUtils {
  val START = "START"
  val STOP = "STOP"
}

case class UpdateCallBackRef(reminder: Reminder, callBackRefUrl: String)

case class UpdateCallBackSuccess(reminder: Reminder)
