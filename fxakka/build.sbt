import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "updater"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.2-1003-jdbc4",
  "play" %% "anorm" % "2.1-SNAPSHOT"
)

libraryDependencies += "io.spray" % "spray-can" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-io" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-client" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-routing" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-util" % "1.1-M8"

libraryDependencies += "io.spray" % "spray-json_2.10" % "1.2.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.1.4"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.1.2"

libraryDependencies += "org.jsoup" % "jsoup" % "1.6.1"

libraryDependencies += "org.specs2" %% "specs2" % "2.2.2" % "test"

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers += Resolver.url("Typesafe Ivy Releases", new java.net.URL("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Patterns("[organisation]/[module]/[revision]/ivy-[revision].xml"::Nil, "[organisation]/[module]/[revision]/[artifact]-[revision].[ext]"::Nil,false) )

resolvers += "Typesafe Snapshots"    at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "spray repo" at "http://repo.spray.io"
