scalaVersion := "2.10.1"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "io.spray" % "spray-client" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-json_2.10" % "1.2.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.4"

libraryDependencies += "com.typesafe.akka" %% "akka-agent" % "2.1.4"

libraryDependencies += "joda-time" % "joda-time" % "2.3"

libraryDependencies += "org.joda" % "joda-convert" % "1.4"

libraryDependencies += "org.specs2" %% "specs2" % "2.2.2" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

scalacOptions in Test ++= Seq("-Yrangepos")

//resolvers += "fakod-snapshots" at "https://raw.github.com/FaKod/fakod-mvn-repo/master/releases"

//resolvers += "fakod-releases" at "https://raw.github.com/FaKod/fakod-mvn-repo/master/releases/"

//libraryDependencies += "org.scala-libs" %% "sjersey-client" % "0.2.0"