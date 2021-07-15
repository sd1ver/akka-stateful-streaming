import sbt._

object Dependencies {

  object Version {
    val Akka = "2.6.13"
  }

  object Testing {
    lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.8" % Test
  }

  object Akka {
    val Slf4j       = "com.typesafe.akka" %% "akka-slf4j"             % Version.Akka
    val Streams     = "com.typesafe.akka" %% "akka-stream"            % Version.Akka
    val Persistence = "com.typesafe.akka" %% "akka-persistence-typed" % Version.Akka

    val all = Seq(Streams, Persistence, Slf4j)
  }

  object Logging {
    val Slf4jApi       = "org.slf4j"                % "slf4j-api"        % "1.7.30"
    val Log4jSlf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.0"
    val all = Seq(Slf4jApi, Log4jSlf4jImpl)
  }
}
