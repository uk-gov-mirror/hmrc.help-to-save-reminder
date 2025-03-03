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

package uk.gov.hmrc.helptosavereminder.models

import play.api.libs.json._
import uk.gov.hmrc.helptosavereminder.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

trait HTSEvent {
  val value: ExtendedDataEvent
}

object HTSEvent {
  def apply(appName: String, auditType: String, detail: JsValue, transactionName: String, path: String)(
    implicit hc: HeaderCarrier): ExtendedDataEvent =
    ExtendedDataEvent(appName, auditType = auditType, detail = detail, tags = hc.toAuditTags(transactionName, path))
}

case class HtsReminderUserDeleted(nino: String, emailAddress: String)

object HtsReminderUserDeleted {
  implicit val format: Format[HtsReminderUserDeleted] = Json.format[HtsReminderUserDeleted]
}

case class HtsReminderUserDeletedEvent(htsReminderUserDeleted: HtsReminderUserDeleted, path: String)(
  implicit hc: HeaderCarrier,
  appConfig: AppConfig)
    extends HTSEvent {

  val value: ExtendedDataEvent = {
    HTSEvent(appConfig.appName, "ReminderDeleted", Json.toJson(htsReminderUserDeleted), "reminder-deleted", path)
  }

}
