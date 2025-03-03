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

package uk.gov.hmrc.helptosavereminder.services.test

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.helptosavereminder.models.test.ReminderGenerator
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderRepository

import scala.concurrent.Future

@Singleton
class TestService @Inject()(htsReminderRepository: HtsReminderRepository) {

  def generateAndInsertReminder(emailPrefix: String, daysToReceive: Seq[Int]): Future[Boolean] =
    htsReminderRepository.updateReminderUser(ReminderGenerator.nextReminder(emailPrefix, daysToReceive))
}
