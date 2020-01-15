package uk.gov.hmrc.helptosavereminder

import scala.concurrent.Future

package object util {

  implicit def toFuture[A](a: A): Future[A] = Future.successful(a)

}
