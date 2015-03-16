
scalaVersion := "2.10.1"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)


libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.4.4"
)