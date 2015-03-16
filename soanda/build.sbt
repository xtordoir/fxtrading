name := "oanda"

organization := "com.impactopia"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.4"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

//libraryDependencies += "io.spray" % "spray-client" % "1.1-M8"
libraryDependencies += "io.spray" % "spray-client" % "1.2.1"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.6"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.4"

libraryDependencies += "com.typesafe.akka" %% "akka-agent" % "2.2.4"

libraryDependencies += "joda-time" % "joda-time" % "2.3"

libraryDependencies += "org.joda" % "joda-convert" % "1.4"

//libraryDependencies += "org.specs2" % "specs2" % "2.2.2" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

scalacOptions in Test ++= Seq("-Yrangepos")

//resolvers += "fakod-snapshots" at "https://raw.github.com/FaKod/fakod-mvn-repo/master/releases"

//resolvers += "fakod-releases" at "https://raw.github.com/FaKod/fakod-mvn-repo/master/releases/"

//libraryDependencies += "org.scala-libs" %% "sjersey-client" % "0.2.0"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))