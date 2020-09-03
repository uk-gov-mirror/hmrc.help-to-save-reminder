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

package uk.gov.hmrc.helptosavereminder.config

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val config: Configuration, val environment: Environment, val servicesConfig: ServicesConfig) {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val appName: String = config.get[String]("appName")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")

  val graphiteHost: String = config.get[String]("microservice.metrics.graphite.host")

  val sendEmailTemplateId: String = config.get[String]("microservice.services.email.templateId")

  val nameParam: String = config.get[String]("microservice.services.email.nameParam")

  val monthParam: String = config.get[String]("microservice.services.email.monthParam")

  val callBackUrlParam: String = config.get[String]("microservice.services.email.callBackUrlParam")

  val isUserScheduleEnabled: Boolean = config.getOptional[Boolean](s"isUserScheduleEnabled").getOrElse(false)

  val userScheduleCronExpression: String = config.getOptional[String](s"userScheduleCronExpression").getOrElse("")

}
