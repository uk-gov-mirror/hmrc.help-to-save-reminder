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

package uk.gov.helptosavereminder.repo

import java.time.LocalDate

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.{DefaultWriteResult, WriteResult}
import uk.gov.hmrc.helptosavereminder.models.Reminder
import uk.gov.hmrc.helptosavereminder.models.test.ReminderGenerator
import uk.gov.hmrc.helptosavereminder.repo.{HtsReminderMongoRepository, HtsReminderRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class HtsReminderRepositorySpec
    extends UnitSpec with MockitoSugar with MongoSpecSupport with GuiceOneAppPerSuite with BeforeAndAfterAll {

  val config = app.injector.instanceOf[Configuration]
  implicit val ec = app.injector.instanceOf[ExecutionContext]
  implicit val hc = HeaderCarrier()
  val env = mock[play.api.Environment]

  val servicesConfig = mock[ServicesConfig]

  implicit val reactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector = mongoConnectorForTest
  }

  val htsReminderMongoRepository = new HtsReminderMongoRepository(reactiveMongoComponent)

  "Calls to create Reminder a HtsReminder repository" should {
    "should successfully create that reminder" in {

      val reminderValue = ReminderGenerator.nextReminder

      val result: Future[Either[String, Reminder]] = htsReminderMongoRepository.createReminder(reminderValue)

      await(result) match {
        case (Right(x)) => x.nino shouldBe reminderValue.nino
      }

    }
  }

  "Calls to findHtsUsersToProcess a HtsReminder repository" should {
    "should successfully find that user" in {

      val reminderValue = ReminderGenerator.nextReminder

      val usersToProcess: Future[Option[List[Reminder]]] = htsReminderMongoRepository.findHtsUsersToProcess()

      await(usersToProcess) match {
        case Some(x) => x.size shouldBe >=(1)

        case None =>
      }

    }
  }

  "Calls to updateNextSendDate a Hts Reminder repository" should {
    "should successfully update NextSendDate " in {

      val reminderValue = ReminderGenerator.nextReminder

      val nextSendDate: Future[Boolean] = htsReminderMongoRepository.updateNextSendDate(reminderValue.nino.toString())

      await(nextSendDate) shouldBe true

    }
  }

  "Calls to updateCallBackRef a Hts Reminder repository" should {
    "should successfully update CallBackRef " in {

      val reminderValue = ReminderGenerator.nextReminder
      val callBackRef = System.currentTimeMillis().toString + reminderValue.nino

      val nextSendDate: Future[Boolean] =
        htsReminderMongoRepository.updateCallBackRef(reminderValue.nino.toString(), callBackRef)

      await(nextSendDate) shouldBe true

    }
  }

  "Calls to updateEmailBounceCount a Hts Reminder repository" should {
    "should successfully update EmailBounceCount " in {

      val reminderValue = ReminderGenerator.nextReminder

      val nextSendDate: Future[Boolean] =
        htsReminderMongoRepository.updateEmailBounceCount(reminderValue.nino.toString())

      await(nextSendDate) shouldBe true

    }
  }
}