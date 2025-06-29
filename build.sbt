import Dependencies.*

ThisBuild / scalaVersion := Dependencies.Versions.scala
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.bdmendes"
ThisBuild / organizationName := "bdmendes"

lazy val root =
  project
    .in(file("."))
    .settings(
      name := "smockito",
      scalafmtOnCompile := true,
      scalacOptions ++=
        Seq(
          "-encoding",
          "utf8",
          "-deprecation",
          "-language:implicitConversions",
          "-Wunused:all",
          "-feature",
          "-release",
          Dependencies.Versions.java
        ),
      libraryDependencies ++= Seq(mockito, munit % Test)
    )
