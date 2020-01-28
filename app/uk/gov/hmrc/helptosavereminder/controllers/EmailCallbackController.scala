package uk.gov.hmrc.helptosavereminder.controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.{Configuration, Environment, Logger}
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, ControllerComponents, MessagesControllerComponents, Result}
import play.libs.ws.WSClient
import uk.gov.hmrc.helptosavereminder.repo.HtsReminderMongoRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class EmailCallbackController  @Inject()(environment: Environment,
                                         val runModeConfiguration: Configuration,
                                         servicesConfig: ServicesConfig,val cc: MessagesControllerComponents,
                                         repository: HtsReminderMongoRepository)(implicit ec: ExecutionContext) extends BackendController(cc){


  def findBounces(callBackRefrenec: String) = Action.async { implicit request =>
    val nino=callBackRefrenec.takeRight(9)
    repository.updateEmailBounceCount(nino).map {
      case true => {
        Logger.info("Updated the User email bounce count for " + nino)
      }

      case _ => //Failure
    }
Future.successful(Ok("Sucess"))
  }

}
