name := """gochal1"""
version := "1.0"

scalaVersion := "2.11.5"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "org.scodec" %% "scodec-bits" % "1.0.5",
  "commons-io" % "commons-io" % "2.4",
  "junit" % "junit" % "4.12" % "test",
  "org.specs2" %% "specs2-core" % "3.1" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")
