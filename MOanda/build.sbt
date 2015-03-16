import AssemblyKeys._ // put this at the top of the file

assemblySettings

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.1.2"

libraryDependencies += "org.jsoup" % "jsoup" % "1.6.1"

//libraryDependencies += "org.scala-tools.testing" %% "scalacheck" % "1.9" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")