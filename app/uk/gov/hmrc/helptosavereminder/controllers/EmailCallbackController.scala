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

package uk.gov.hmrc.helptosavereminder.controllers

import com.google.inject.Inject
import play.api.{Configuration, Environment, Logger}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.helptosavereminder.models.ActorUtils._

import scala.concurrent.{ExecutionContext, Future}

class EmailCallbackController @Inject()(
  environment: Environment,
  val runModeConfiguration: Configuration,
  servicesConfig: ServicesConfig,
  val cc: MessagesControllerComponents,
  repository: HtsReminderMongoRepository)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def findBounces(callBackRefrenec: String) = Action.async { implicit request =>
    val nino = callBackRefrenec.takeRight(9)
    repository.updateEmailBounceCount(nino).map {
      case true => {
        Logger.info("Updated the User email bounce count for " + nino)
        Ok(SUCCESS)
      }

      case _ => {
        Ok(FAILURE)
      }
    }

  }

}
