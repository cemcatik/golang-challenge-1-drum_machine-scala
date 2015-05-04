name := "golang-challenge-scala"

val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq(
    "-feature",
    "-language:implicitConversions"
  ),
  scalacOptions in Test ++= Seq("-Yrangepos"),
  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  fork in Test := true
)

def challenge(name: String) = Project(s"golang-challenge-$name", file(name)).
  settings(commonSettings: _*)

lazy val challenges = (project in file(".")).
  settings(commonSettings: _*).
  aggregate(challenge1, challenge2)

lazy val challenge1 = (challenge("1-drum_machine")).
  settings(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
      "commons-io" % "commons-io" % "2.4",
      "org.specs2" %% "specs2-core" % "3.6" % "test"
    )
  )

lazy val challenge2 = (challenge("2-nacl")).
  settings(
    libraryDependencies ++= Seq(
      "org.abstractj.kalium" % "kalium" % "0.3.0",
      "commons-io" % "commons-io" % "2.4",
      "com.github.scopt" %% "scopt" % "3.3.0",
      "org.specs2" %% "specs2-core"       % "3.6" % "test",
      "org.specs2" %% "specs2-scalacheck" % "3.6" % "test"
    )
  )
