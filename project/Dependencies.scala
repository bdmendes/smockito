import sbt.*

object Dependencies {

  object Versions {
    // We use the latest Scala LTS version, as advised for libraries.
    lazy val scala = "3.3.6"
    lazy val java = "21"
    lazy val mockito = "5.18.0"
  }

  lazy val mockito = "org.mockito" % "mockito-core" % Versions.mockito
  lazy val munit = "org.scalameta" %% "munit" % "1.1.1"
}
