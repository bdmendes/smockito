import Dependencies.*

ThisBuild / scalaVersion := Dependencies.Versions.scala
ThisBuild / version := "0.1.0-rc1"
ThisBuild / organization := "com.bdmendes"
ThisBuild / homepage := Some(url("https://github.com/bdmendes/smockito"))
ThisBuild / description := "Tiny Scala facade for Mockito."

ThisBuild / developers :=
  List(Developer("bdmendes", "Bruno Mendes", "bd_mendes@outlook.com", url("https://bdmendes.com")))

lazy val root =
  project
    .in(file("."))
    .enablePlugins(JavaAgent)
    .settings(
      name := "smockito",
      autoAPIMappings := true,
      scalafmtOnCompile := true,
      javaAgents := Seq(mockito % Test),
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
