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

import cats.instances.string._
import cats.syntax.eq._
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.helptosave.controllers.HtsReminderAuth
import uk.gov.hmrc.helptosavereminder.audit.HTSAuditor
import uk.gov.hmrc.helptosavereminder.config.AppConfig
import uk.gov.hmrc.helptosavereminder.models._
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderRepository
import uk.gov.hmrc.helptosavereminder.util.JsErrorOps._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HtsUserUpdateController @Inject()(
  repository: HtsReminderRepository,
  cc: ControllerComponents,
  auditor: HTSAuditor,
  override val authConnector: AuthConnector)(implicit val ec: ExecutionContext, appConfig: AppConfig)
    extends HtsReminderAuth(authConnector, cc) {

  val notAllowedThisNino: Future[Result] =
    Future.successful(Forbidden("You can't access a Nino that isn't associated with the one you're logged in with"))

  def update(): Action[AnyContent] = ggAuthorisedWithNino { implicit request => implicit nino ⇒
    request.body.asJson.map(_.validate[HtsUserSchedule]) match {

      case Some(JsSuccess(htsUser, _)) if htsUser.nino.nino === nino ⇒ {
        Logger.debug(s"The HtsUser received from frontend to update is : ${htsUser.nino.value}")
        repository.updateReminderUser(htsUser).map {
          case true => {
            val path = routes.HtsUserUpdateController.update().url
            auditor.sendEvent(
              HtsReminderUserUpdatedEvent(HtsReminderUserUpdated(htsUser.nino.value, Json.toJson(htsUser)), path),
              htsUser.nino.value)
            Ok(Json.toJson(htsUser))
          }
          case false => NotModified
        }
      }

      case Some(JsSuccess(_, _)) => notAllowedThisNino

      case Some(error: JsError) ⇒
        val errorString = error.prettyPrint()
        Logger.warn(s"Could not parse HtsUser JSON in request body: $errorString")
        Future.successful(BadRequest(s"Could not parse HtsUser JSON in request body: $errorString"))

      case None ⇒
        Logger.warn("No JSON body found in request")
        Future.successful(BadRequest(s"No JSON body found in request"))
    }
  }

  def getHtsUser(nino: String): Action[AnyContent] = ggAuthorisedWithNino { implicit request => implicit authNino =>
    if (nino === authNino) {
      repository.findByNino(nino).map {
        case Some(htsUser) => Ok(Json.toJson(htsUser))
        case None          => NotFound
      }
    } else notAllowedThisNino
  }

  def deleteHtsUser(): Action[AnyContent] = ggAuthorisedWithNino { implicit request => implicit nino ⇒
    request.body.asJson.map(_.validate[CancelHtsUserReminder]) match {
      case Some(JsSuccess(userReminder, _)) ⇒ {
        Logger.debug(s"The HtsUser received from frontend to delete is : ${userReminder.nino}")
        repository.deleteHtsUser(userReminder.nino).map {
          case Right(()) => {
            val path = routes.HtsUserUpdateController.deleteHtsUser().url
            auditor.sendEvent(
              HtsReminderUserDeletedEvent(HtsReminderUserDeleted(userReminder.nino, Json.toJson(userReminder)), path),
              userReminder.nino)
            Ok
          }
          case Left(error) => NotModified
        }
      }

      case Some(error: JsError) ⇒
        val errorString = error.prettyPrint()
        Logger.warn(s"Could not parse CancelHtsUserReminder JSON in request body: $errorString")
        Future.successful(BadRequest(s"Could not parse CancelHtsUserReminder JSON in request body: $errorString"))

      case None ⇒
        Logger.warn("No JSON body found in request")
        Future.successful(BadRequest(s"No JSON body found in request"))
    }
  }

  def updateEmail(): Action[AnyContent] = ggAuthorisedWithNino { implicit request => implicit nino ⇒
    request.body.asJson.map(_.validate[UpdateEmail]) match {
      case Some(JsSuccess(userReminder, _)) ⇒ {
        Logger.debug(s"The HtsUser received from frontend to update is : ${userReminder.nino.value}")
        repository
          .updateEmail(userReminder.nino.value, userReminder.firstName, userReminder.lastName, userReminder.email)
          .map {
            case true  => Ok
            case false => NotFound
          }
      }

      case Some(error: JsError) ⇒
        val errorString = error.prettyPrint()
        Logger.warn(s"Could not parse UpdateEmail JSON in request body: $errorString")
        Future.successful(BadRequest(s"Could not parse UpdateEmail JSON in request body:: $errorString"))

      case None ⇒
        Logger.warn("No JSON body found in request")
        Future.successful(BadRequest(s"No JSON body found in request"))
    }
  }

}
