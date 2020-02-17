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

import java.time.LocalDate

import com.google.inject.ImplementedBy
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsBoolean, JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.collections.GenericCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.helptosavereminder.models.HtsUser
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import reactivemongo.play.json.ImplicitBSONHandlers._
import reactivemongo.play.json.JSONSerializationPack
import uk.gov.hmrc.domain.Nino

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[HtsReminderMongoRepository])
trait HtsReminderRepository {
  def createReminder(reminder: HtsUser): Future[Either[String, HtsUser]]
  def findHtsUsersToProcess(): Future[Option[List[HtsUser]]]
  def updateNextSendDate(nino: String): Future[Boolean]
  def updateEmailBounceCount(nino: String): Future[Boolean]
  def updateCallBackRef(nino: String, callBackRef: String): Future[Boolean]
  def updateReminderUser(htsReminder: HtsUser): Future[Boolean]
  def findByNino(nino: String): Future[Option[HtsUser]]

}

class HtsReminderMongoRepository @Inject()(mongo: ReactiveMongoComponent)
    extends ReactiveRepository[HtsUser, BSONObjectID](
      collectionName = "help-to-save-reminder",
      mongo = mongo.mongoConnector.db,
      HtsUser.htsUserFormat,
      ReactiveMongoFormats.objectIdFormats
    ) with HtsReminderRepository {

  lazy val proxyCollection: GenericCollection[JSONSerializationPack.type] = collection

  override def createReminder(reminder: HtsUser): Future[Either[String, HtsUser]] =
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

  override def findHtsUsersToProcess(): Future[Option[List[HtsUser]]] = {

    val startTime = System.currentTimeMillis()

    val testResult = Try {

      val usersToProcess: Future[List[HtsUser]] = proxyCollection
        .find(Json.obj(), Option.empty[JsObject])
        .sort(Json.obj("nino" -> 1))
        .cursor[HtsUser](ReadPreference.primary)
        .collect[List](-1, Cursor.FailOnError[List[HtsUser]]())

      usersToProcess onComplete {
        case _ => //Log the time
      }
      usersToProcess

    }

    testResult match {
      case Success(usersList) => {
        usersList.map(x => Some(x))
      }

      case Failure(f) => {
        Logger.error(s"[HtsReminderMongoRepository][findHtsUsersToProcess] failed: ${f.getMessage}")
        //metrics.findRequestsToProcessTimer(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
        Future.successful(None)
      }
    }

  }

  override def updateNextSendDate(nino: String): Future[Boolean] = {

    val startTime = System.currentTimeMillis()
    val selector = Json.obj("nino" -> nino)
    val modifier = Json.obj("$set" -> Json.obj("nextSendDate" -> LocalDate.now()))
    val result = proxyCollection.update(ordered = false).one(selector, modifier)

    Logger.info("Entered the updateNextSendDate for user nino = " + nino)

    result
      .map { status =>
        Logger.debug(s"[HtsReminderMongoRepository][updateNextSendDate] updated:, result : $status ")
        status.ok
      }
      .recover {
        // $COVERAGE-OFF$
        case e =>
          Logger.error("Failed to update HtsUser", e)
          false
        // $COVERAGE-ON$
      }

  }

  override def updateCallBackRef(nino: String, callBackRef: String): Future[Boolean] = {

    val startTime = System.currentTimeMillis()
    val selector = Json.obj("nino" -> nino)
    val modifier = Json.obj("$set" -> Json.obj("callBackUrlRef" -> callBackRef))

    val result = proxyCollection.update(ordered = false).one(selector, modifier)

    result
      .map { status =>
        Logger.debug(s"[HtsReminderMongoRepository][updateCallBackRef] updated:, result : $status ")
        status.ok
      }
      .recover {
        // $COVERAGE-OFF$
        case e =>
          Logger.error("Failed to update HtsUser", e)
          false
        // $COVERAGE-ON$
      }

  }

  override def updateEmailBounceCount(nino: String): Future[Boolean] = {

    val selector = Json.obj("nino" -> nino)
    val modifier = Json.obj("$inc" -> Json.obj("bounceCount" -> 1))

    val result = proxyCollection.update(ordered = false).one(selector, modifier)

    result
      .map { status =>
        Logger.debug(s"[HtsReminderMongoRepository][updateEmailBounceCount] incremented:, result : $status ")
        status.ok
      }
      .recover {
        // $COVERAGE-OFF$
        case e =>
          Logger.error("Failed to update HtsUser", e)
          false
        // $COVERAGE-ON$
      }

  }

  override def updateReminderUser(htsReminder: HtsUser): Future[Boolean] = {

    val selector = Json.obj("nino" -> htsReminder.nino.nino)
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "optInStatus"   -> JsBoolean(htsReminder.optInStatus),
        "email"         -> htsReminder.email,
        "name"          -> htsReminder.name,
        "daysToReceive" -> htsReminder.daysToReceive))

    val result = proxyCollection.update(ordered = false).one(selector, modifier)

    result
      .map { status =>
        Logger.debug(s"[HtsReminderMongoRepository][updateReminderUser] updated:, result : $status ")
        if (status.n == 0) {
          createReminder(htsReminder)
          Logger.debug(s"[HtsReminderMongoRepository][updateReminderUser] new user created: $status ")
        } else {
          Logger.debug(s"[HtsReminderMongoRepository][updateReminderUser] updated:, result : $status ")
        }
        status.ok
      }
      .recover {
        // $COVERAGE-OFF$
        case e =>
          Logger.error("Failed to update HtsUser", e)
          false
        // $COVERAGE-ON$
      }

  }

  override def findByNino(nino: String): Future[Option[HtsUser]] = {

    val tryResult = Try {
      proxyCollection
        .find(Json.obj("nino" -> nino), Option.empty[JsObject])
        .cursor[HtsUser](ReadPreference.primary)
        .collect[List](maxDocs = 1, err = Cursor.FailOnError[List[HtsUser]]())
    }

    tryResult match {
      case Success(s) => {
        s.map { x =>
          Logger.debug(s"[HtsReminderMongoRepository][findByNino] : { request : $nino, result: $x }")
          x.headOption
        }
      }
      case Failure(f) => {
        Logger.debug(s"[HtsReminderMongoRepository][findByNino] : { request : $nino, exception: ${f.getMessage} }")
        Future.successful(None)
      }
    }
  }

  override def indexes: Seq[Index] = Seq(
    Index(Seq("nino" -> IndexType.Ascending), Some("nino"), background = true)
  )

}
