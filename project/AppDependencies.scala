import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "simple-reactivemongo"     % "7.21.0-play-26",
    "org.reactivemongo"       %% "reactivemongo-iteratees"  % "0.18.6",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.3.0",
    "uk.gov.hmrc"             %% "mongo-lock"               % "6.15.0-play-26"
  )


  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.3.0"                 % Test classifier "tests",
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.15.0-play-26",
    "org.reactivemongo"       %% "reactivemongo-iteratees"  % "0.18.6",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test",
    //"com.typesafe.akka"       %% "akka-testkit"             % "2.5.26",
    "com.typesafe.play"       %% "play-test"                % current                 % "test",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it"
  )

}
