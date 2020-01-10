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

package uk.gov.hmrc.helptosavereminder.repo

import com.google.inject.ImplementedBy
import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.helptosavereminder.models.Reminder
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

@ImplementedBy(classOf[MongoReminderStore])
trait ReminderStore {

}

class MongoReminderStore @Inject() (mongo: ReactiveMongoComponent)
  extends ReactiveRepository[Reminder, BSONObjectID](
    collectionName = "reminders",
    mongo = mongo.mongoConnector.db,
    Reminder.reminderFormat,
    ReactiveMongoFormats.objectIdFormats
  ) with ReminderStore {

}
