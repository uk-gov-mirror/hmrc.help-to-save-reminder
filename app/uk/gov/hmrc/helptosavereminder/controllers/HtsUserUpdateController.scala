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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.helptosavereminder.auth.HtsReminderAuth
import uk.gov.hmrc.helptosavereminder.controllers.HelpToSaveReminderController.GetHtsUserResponse
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.helptosavereminder.models.HtsUser
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HtsUserUpdateController @Inject()(
  htsReminderAuth: HtsReminderAuth,
  repository: HtsReminderMongoRepository,
  cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends BackendController(cc) {

  def update(): Action[AnyContent] = htsReminderAuth.ggAuthorisedWithNino { implicit request => implicit nino ⇒
    request.body.asJson.get
      .validate[HtsUser]
      .fold(
        error => {
          Logger.error(s"Unable to de-serialise request as a HtsUser: ${error.mkString}")
          Future.successful(BadRequest)
        },
        (hstUser: HtsUser) => {
          Logger.info(s"The HtsUser received from frontend to update is : " + hstUser)
          repository.updateReminderUser(hstUser).map {
            case true  => Ok(Json.toJson(GetHtsUserResponse("SUCCESS")))
            case false => NotModified
          }
        }
      )
  }

}

object HelpToSaveReminderController {

  private[controllers] case class GetHtsUserResponse(updateStatus: String)

  private[controllers] object GetHtsUserResponse {
    implicit val format: Format[GetHtsUserResponse] = Json.format[GetHtsUserResponse]
  }

}
