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

package uk.gov.hmrc.helptosavereminder.actors

import akka.actor.{Actor, ActorRef, Props}
import javax.inject.Singleton
import play.api.Logger
import uk.gov.hmrc.helptosavereminder.models.Reminder

import scala.concurrent.ExecutionContext

@Singleton
class EmailSenderActor(val mongoApi: play.modules.reactivemongo.ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends Actor {

  lazy val htsUserUpdateActor: ActorRef =
    context.actorOf(Props(classOf[HtsUserUpdateActor], mongoApi, ec), "htsUserUpdate-actor")

  override def receive: Receive = {
    case htsUserReminder: Reminder => {

      Logger.info("User to process is " + htsUserReminder.nino)
      //TODO: If response from digital contact = 202 then update reminder in mongo to have a new next send date

      htsUserUpdateActor ! htsUserReminder

    }

  }
}
