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

package uk.gov.hmrc.helptosavereminder.connectors

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helptosavereminder.models.SendTemplatedEmailRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class EmailConnector @Inject()(http: HttpClient) {

  def sendEmail(request: SendTemplatedEmailRequest, url: String)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Boolean] =
    http.POST(url, request, Seq(("Content-Type", "application/json"))) map { response =>
      response.status match {
        case 202 => Logger.debug(s"[EmailSenderActor] Email sent: ${response.body}"); true
        case _   => Logger.error(s"[EmailSenderActor] Email not sent: ${response.body}"); false
      }
    }

  def unBlockEmail(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    http
      .DELETE(url, Seq(("Content-Type", "application/json")))
      .onComplete({
        case Success(response) =>
          Future.successful(s"An unblock request to Email Service is sent")
        case Failure(ex) =>
          Logger.error(s"Email Service could not unblock email for user Nino = and exception is $ex")
          Future.failed(new Exception("Email Service could not unblock email for user Nino"))
      })
    Future.successful(s"Email Service successfully unblocked email")
  }
}
