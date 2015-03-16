com.twitter.scrooge.ScroogeSBT.newSettings

scalaVersion := "2.10.1"

lazy val soanda = project.in( file("soanda") )

lazy val hff = project.in( file("hff")).dependsOn(soanda)

lazy val fxthriftkafka = project.in(file("fx-thrift-kafka")).dependsOn(soanda, hff)

lazy val fx4j = project.in(file("fx4j")).dependsOn(hff)

lazy val fxmanager = project.in(file("fxmanager")).dependsOn(soanda, hff)

lazy val ticksfeed = project.in(file("ticksfeed")).dependsOn(soanda, fxthriftkafka)

lazy val ticksfeedReader = project.in(file("ticksfeedReader")).dependsOn(soanda, fxthriftkafka)

//lazy val moanda = project.in( file("MOanda") )

//lazy val fxheatmap = project in file("FXHeatmapClient")

//lazy val fxakka = project.dependsOn(moanda, fxheatmap)