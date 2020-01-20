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
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.collections.GenericCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.helptosavereminder.models.Reminder
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.JSONSerializationPack

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[HtsReminderMongoRepository])
trait HtsReminderRepository {
  def createReminder(reminder: Reminder): Future[Either[String, Reminder]]
  def findHtsUsersToProcess(): Future[Option[List[Reminder]]]
}

class HtsReminderMongoRepository @Inject()(mongo: ReactiveMongoComponent)
    extends ReactiveRepository[Reminder, BSONObjectID](
      collectionName = "help-to-save-reminder",
      mongo = mongo.mongoConnector.db,
      Reminder.reminderFormat,
      ReactiveMongoFormats.objectIdFormats
    ) with HtsReminderRepository {

  lazy val proxyCollection: GenericCollection[JSONSerializationPack.type] = collection

  override def createReminder(reminder: Reminder): Future[Either[String, Reminder]] =
    insert(reminder)
      .map(result =>
        if (result.ok) {
          Right(reminder)
        } else {
          Left(
            WriteResult
              .lastError(result)
              .flatMap(lastError => lastError.errmsg.map(identity))
              .getOrElse("Unexpected error while creating Reminder"))
      })

  override def findHtsUsersToProcess(): Future[Option[List[Reminder]]] = {

    val startTime = System.currentTimeMillis()

    val testResult = Try {

      val usersToProcess = proxyCollection
        .find(Json.obj(), Option.empty[JsObject])
        .sort(Json.obj("_id" -> 1))
        .cursor[Reminder](ReadPreference.primary)
        .collect[List](-1, Cursor.FailOnError[List[Reminder]]())

    }

    testResult match {
      case Success(s) => {
        Future.successful(Some(List.empty))
      }

      case Failure(f) => {
        Logger.error(s"[BulkCalculationRepository][findRequestsToProcess] failed: ${f.getMessage}")
        //metrics.findRequestsToProcessTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
        Future.successful(None)
      }
    }

  }

}
