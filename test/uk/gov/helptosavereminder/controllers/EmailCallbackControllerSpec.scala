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

package uk.gov.helptosavereminder.controllers

import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.{FakeRequest, Helpers}
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.helptosavereminder.config.AppConfig
import uk.gov.hmrc.helptosavereminder.controllers.EmailCallbackController
import uk.gov.hmrc.helptosavereminder.models.Reminder
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailCallbackControllerSpec extends UnitSpec with Matchers with ScalaFutures with GuiceOneAppPerSuite with MockitoSugar {

  val fakeGetRequest = FakeRequest(Helpers.GET, "/bounce/100382SN123456B")

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(configuration, new RunMode(configuration, Mode.Dev))
  private val appConfig = new AppConfig(configuration, serviceConfig)
  var runMode = mock[RunMode]
  lazy val mockRepository = mock[HtsReminderMongoRepository]

  private val controller = new EmailCallbackController(env, configuration, serviceConfig, Helpers.stubControllerComponents(), mockRepository)

  "The EmailCallbackController" should {
    "be able to increment a bounce count and" should {
      "respond with a 200 when all is good" in {
        val callBackRefrenece = "100382SN123456B"
        when(mockRepository.updateEmailBounceCount(any(classOf[Reminder]))).thenReturn(Future.successful(200))
        val result = controller.findBounces(callBackRefrenece)
        result shouldBe Status.200
      }
    }
  }
}