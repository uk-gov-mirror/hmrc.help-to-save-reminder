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

package uk.gov.hmrc.helptosavereminder.auth

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.helptosave.util.{NINO, toFuture}
import uk.gov.hmrc.auth.core.AuthProvider.{GovernmentGateway, PrivilegedApplication}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{nino ⇒ v2Nino}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HtsReminderAuth @Inject()(val microServiceAuthConnector: DefaultAuthConnector, cc: ControllerComponents)(
  implicit val ec: ExecutionContext)
    extends BackendController(cc) with AuthorisedFunctions {
  override def authConnector: AuthConnector = microServiceAuthConnector

  private val AuthProvider: AuthProviders = AuthProviders(GovernmentGateway)
  private type AuthAction[A] = Request[A] => Future[Result]

  private type HtsAction = Request[AnyContent] ⇒ Future[Result]
  private type HtsActionWithNINO = Request[AnyContent] ⇒ NINO ⇒ Future[Result]

  val authProviders: AuthProviders = AuthProviders(GovernmentGateway, PrivilegedApplication)

  val GGProvider: AuthProviders = AuthProviders(GovernmentGateway)

  val GGAndPrivilegedProviders: Predicate = AuthProviders(GovernmentGateway, PrivilegedApplication)

  val AuthWithCL200: Predicate = GGProvider and ConfidenceLevel.L200

  private def isAgentOrOrganisation(group: AffinityGroup): Boolean =
    group.toString.contains("Agent") || group.toString.contains("Organisation")

  def authHtsReminder(action: AuthAction[AnyContent]): Action[AnyContent] = Action.async { implicit request ⇒
    authCommon(action)
  }

  def authHtsReminderWithJson(action: AuthAction[JsValue], json: BodyParser[JsValue]): Action[JsValue] =
    Action.async(json) { implicit request ⇒
      authCommon(action)
    }

  def authCommon[A](action: AuthAction[A])(implicit request: Request[A]): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(request.headers)
    authorised(AuthProvider)
      .retrieve(affinityGroup) {
        case Some(affinityG) if isAgentOrOrganisation(affinityG) ⇒ action(request)
        case _ => Future.successful(Unauthorized)
      }
      .recover[Result] {
        case e: NoActiveSession => Unauthorized(e.reason)
      }
  }

  def ggAuthorisedWithNino(action: HtsActionWithNINO)(implicit ec: ExecutionContext): Action[AnyContent] =
    Action.async { implicit request ⇒
      authorised(AuthWithCL200)
        .retrieve(v2Nino) { mayBeNino ⇒
          mayBeNino.fold[Future[Result]] {
            Forbidden
          }(nino ⇒ action(request)(nino))
        }
        .recover {
          handleFailure()
        }
    }

  def handleFailure(): PartialFunction[Throwable, Result] = {
    case _: NoActiveSession ⇒
      //logger.warn("user is not logged in, probably a hack?")
      Unauthorized

    case e: InternalError ⇒
      //logger.warn(s"Could not authenticate user due to internal error: ${e.reason}")
      InternalServerError

    case ex: AuthorisationException ⇒
      //logger.warn(s"could not authenticate user due to: ${ex.reason}")
      Forbidden
  }
}
