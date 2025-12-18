import Dependencies.*
import ReleaseTransformations.*

ThisBuild / organization := "com.bdmendes"
ThisBuild / homepage := Some(url("https://github.com/bdmendes/smockito"))
ThisBuild / description := "Tiny Scala facade for Mockito."
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      url("https://github.com/bdmendes/smockito"),
      "scm:git:ssh://git@github.com/bdmendes/smockito.git"
    )
  )

ThisBuild / developers :=
  List(Developer("bdmendes", "Bruno Mendes", "bd_mendes@outlook.com", url("https://bdmendes.com")))

lazy val root =
  project
    .in(file("."))
    .enablePlugins(JavaAgent)
    .settings(
      name := "smockito",
      scalaVersion := Dependencies.Versions.scala,
      javaAgents := Seq(mockito % Test),
      scalacOptions ++=
        Seq(
          "-encoding",
          "utf8",
          "-deprecation",
          "-language:implicitConversions",
          "-Xfatal-warnings",
          "-Wunused:all",
          "-feature",
          "-release",
          Dependencies.Versions.java,
          // Workaround for https://github.com/scala/scala3/issues/23967.
          "-Wconf:origin=scala.compiletime.testing.*:s"
        ),
      libraryDependencies ++= Seq(mockito, munit % Test),
      Compile / packageBin / packageOptions +=
        Package.ManifestAttributes(
          "Premain-Class" -> "com.bdmendes.smockito.internal.MockitoAgent",
          "Can-Redefine-Classes" -> "true",
          "Can-Retransform-Classes" -> "true"
        ),
      publishTo := {
        val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
        if (isSnapshot.value)
          Some("central-snapshots".at(centralSnapshots))
        else
          localStaging.value
      },
      Compile / doc / scalacOptions ++=
        Seq("-siteroot", "docs", "-social-links:github::https://github.com/bdmendes/smockito"),
      autoAPIMappings := true,
      publishMavenStyle := true,
      Test / publishArtifact := false,
      pomIncludeRepository := (_ => false)
    )

releaseTagComment := s"Release ${(ThisBuild / version).value}"
releaseCommitMessage := s"Bump version to ${(ThisBuild / version).value}"
releaseNextCommitMessage := s"Bump version to ${(ThisBuild / version).value}"

releaseProcess :=
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("publishSigned"),
    releaseStepCommand("sonaRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
