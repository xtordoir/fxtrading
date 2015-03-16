
import AssemblyKeys._

libraryDependencies += "com.novocode" % "junit-interface" % "0.10-M4" % "test"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

assemblySettings