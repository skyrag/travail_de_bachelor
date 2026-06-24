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
      "org.testcontainers" % "testcontainers" % "1.21.4" % "test",
      "org.testcontainers" % "postgresql" % "1.21.4" % "test",
      "org.junit.jupiter" % "junit-jupiter" % "5.10.2" % "test",
      "org.jacoco" % "org.jacoco.core"  % "0.8.12",
      "org.jacoco" % "org.jacoco.report" % "0.8.12",
      "org.jacoco" % "org.jacoco.agent"  % "0.8.12"
    ),
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v"),
    scalacOptions ++= List("-feature", "-Werror"),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation", "-Werror"),
    PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml",

  )


scalaVersion := "2.13.18"
