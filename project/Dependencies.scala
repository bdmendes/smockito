import sbt.*

object Dependencies {

  object Versions {
    // We use the latest Scala LTS version, as advised for libraries.
    lazy val scala = "3.3.6"
    // We want to be conservative with the Java version, but not too much.
    lazy val java = "17"
    lazy val mockito = "5.20.0"
  }

  lazy val mockito = "org.mockito" % "mockito-core" % Versions.mockito
  lazy val munit = "org.scalameta" %% "munit" % "1.2.0"
}
