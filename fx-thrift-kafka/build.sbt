name := "fx-thrift"

organization := "com.impactopia"

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.10.4"

com.twitter.scrooge.ScroogeSBT.newSettings

libraryDependencies ++= Seq(
  "org.apache.thrift" % "libthrift" % "0.8.0",
  "com.twitter" %% "scrooge-core" % "3.15.0",
  "com.twitter" %% "finagle-thrift" % "6.5.0",
  "org.slf4j" % "slf4j-simple" % "1.6.4",
//
  "net.sf.jopt-simple" % "jopt-simple" % "4.5",
  "org.apache.kafka" % "kafka_2.10" % "0.8.1.1" exclude("javax.jms", "jms") exclude("com.sun.jdmk", "jmxtools") exclude("com.sun.jmx", "jmxri"),
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.4"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))