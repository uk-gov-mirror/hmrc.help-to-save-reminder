import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.31.0-play-27",
    "org.reactivemongo"       %% "reactivemongo-iteratees"  % "0.18.8", /* fix at 0.18.8 because that is what simple-reactivemongo 7.31.0-play-27 uses*/
    "org.reactivemongo"       %% "play2-reactivemongo"      % "0.18.8-play27", /* see above */
    "uk.gov.hmrc"             %% "bootstrap-backend-play-27"% "3.3.0",
    "uk.gov.hmrc"             %% "domain"                   % "5.10.0-play-27",
    "uk.gov.hmrc"             %% "mongo-lock"               % "6.24.0-play-27",
    "org.typelevel"           %% "cats-core"                % "2.3.1",
    "com.enragedginger"       %% "akka-quartz-scheduler"    % "1.8.1-akka-2.5.x"
  )


  val test = Seq(
    "uk.gov.hmrc"             %% "hmrctest"                 % "3.10.0-play-26"        % "test, it",
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.22.0-play-27"        % "test, it",
    "org.mockito"             %  "mockito-all"              % "1.10.19"               % "test, it",
    "com.typesafe.akka"       %% "akka-testkit"             % "2.5.31"                % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.3"                 % "test, it",
    "org.scalatest"           %% "scalatest"                % "3.0.9"                 % "test, it",
    "org.scalamock"           %% "scalamock"                % "5.1.0"                 % "test, it"
  )

}
