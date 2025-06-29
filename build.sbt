import Dependencies.*

ThisBuild / scalaVersion := Dependencies.Versions.scala
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.bdmendes"
ThisBuild / organizationName := "bdmendes"

lazy val root = (project in file("."))
  .settings(
    name := "smockito",
    scalafmtOnCompile := true,
    scalacOptions ++= Seq(
      "-language:implicitConversions",
      "-Wunused:all",
      "-rewrite",
      "-indent",
      "-feature",
      "-release",
      Dependencies.Versions.java
    ),
    libraryDependencies ++= Seq(
      mockito,
      munit % Test
    )
  )
