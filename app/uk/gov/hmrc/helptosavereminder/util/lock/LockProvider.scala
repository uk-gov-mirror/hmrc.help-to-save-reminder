package uk.gov.hmrc.helptosavereminder.util.lock

import uk.gov.hmrc.lock.ExclusiveTimePeriodLock

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

// $COVERAGE-OFF$
trait LockProvider {

  val lockId: String

  val holdLockFor: FiniteDuration

  def releaseLock(): Future[Unit]

  def tryToAcquireOrRenewLock[T](body: ⇒ Future[T])(implicit ec: ExecutionContext): Future[Option[T]]

}

object LockProvider {

  /**
   * This lock provider ensures that some operation is only performed once across multiple
   * instances of an application. It is backed by [[ExclusiveTimePeriodLock]] from the
   * `mongo-lock` library
   */
  case class ExclusiveTimePeriodLockProvider(lock: ExclusiveTimePeriodLock) extends LockProvider {

    val lockId: String = lock.lockId

    val serverId: String = lock.serverId

    val holdLockFor: FiniteDuration = lock.holdLockFor.getMillis.millis

    override def releaseLock(): Future[Unit] =
      lock.repo.releaseLock(lockId, serverId)

    def tryToAcquireOrRenewLock[T](body: ⇒ Future[T])(implicit ec: ExecutionContext): Future[Option[T]] =
      lock.tryToAcquireOrRenewLock(body)
  }

}
// $COVERAGE-ON$
