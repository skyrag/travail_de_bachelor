name := """play-java-seed"""
organization := "autoBattler"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, JacocoPlugin)
  .settings(
    name := """play-auto-battler""",
    version := "0.1",
    libraryDependencies ++= Seq(
      guice,
      javaJpa,
      "org.hibernate" % "hibernate-core" % "6.6.20.Final",
      "org.postgresql" % "postgresql" % "42.7.7",
      "io.hypersistence" % "hypersistence-utils-hibernate-63" % "3.7.0",
      "de.mkammerer" % "argon2-jvm" % "2.11",

      "org.testcontainers" % "testcontainers" % "1.21.4" % "test",
      "org.testcontainers" % "postgresql" % "1.21.4" % "test",
      "org.junit.jupiter" % "junit-jupiter" % "5.10.2" % "test",
      javaWs % "test",
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % "1.0.3" % Test,
      "org.awaitility" % "awaitility" % "4.3.0" % "test",
      "org.assertj" % "assertj-core" % "3.27.3" % "test",
      "org.mockito" % "mockito-core" % "5.18.0" % "test",
    ),
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    scalacOptions ++= List("-feature", "-Werror"),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror"),
    PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml",
    jacocoExcludes := Seq(
      "router.*",
      "*ReverseRoutes*"
    ),

  )


scalaVersion := "2.13.18"
