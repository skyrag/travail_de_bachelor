name := """play-java-seed"""
organization := "autoBattler"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """play-auto-battler""",
    version := "0.1",
    libraryDependencies ++= Seq(
      guice,
      javaJpa,
      "org.hibernate" % "hibernate-core" % "6.6.20.Final",
      "org.postgresql" % "postgresql" % "42.7.7"
    ),
    PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml"
  )


scalaVersion := "2.13.18"
