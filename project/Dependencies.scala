import sbt._

object Dependencies {

  object Version {
    val Akka = "2.6.15"
  }

  object Testing {
    lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.8" % Test
  }

  object Serialization{
    val Spray = "io.spray" %%  "spray-json" % "1.3.6"
  }

  object Akka {
    val Slf4j               = "com.typesafe.akka"      %% "akka-slf4j"                         % Version.Akka
    val Streams             = "com.typesafe.akka"      %% "akka-stream"                        % Version.Akka
    val Persistence         = "com.typesafe.akka"      %% "akka-persistence-typed"             % Version.Akka
    val ClusterSharding     = "com.typesafe.akka"      %% "akka-cluster-sharding-typed"        % Version.Akka
    val KryoSerialization   = "io.altoo"               %% "akka-kryo-serialization"            % "2.0.1"
    val StreamKafka         = "com.typesafe.akka"      %% "akka-stream-kafka"                  % "2.1.0"
    val StreamKafkaSharding = "com.typesafe.akka"      %% "akka-stream-kafka-cluster-sharding" % "2.1.0"
    val MongoPersistence    = "com.github.scullxbones" %% "akka-persistence-mongo-scala"       % "3.0.6"

    val all = Seq(
      Streams,
      Persistence,
      Slf4j,
      ClusterSharding,
      KryoSerialization,
      StreamKafka,
      StreamKafkaSharding,
      MongoPersistence
    )
  }

  object Logging {
    val Slf4jApi       = "org.slf4j"                % "slf4j-api"        % "1.7.30"
    val Log4jSlf4jImpl = "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.13.0"
    val all            = Seq(Slf4jApi, Log4jSlf4jImpl)
  }
}
