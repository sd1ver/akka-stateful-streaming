import Dependencies._

ThisBuild / scalaVersion     := "2.13.6"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ru.neoflex"
ThisBuild / organizationName := "neoflex"

lazy val root = (project in file("."))
  .settings(
    name := "akka-stateful-streaming",
    libraryDependencies += Testing.scalaTest,
    libraryDependencies ++= Akka.all,
    libraryDependencies ++= Logging.all,
    libraryDependencies += Serialization.Spray
  )
