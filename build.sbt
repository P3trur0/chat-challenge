name := "chat-challenge"
version := "0.1"
scalaVersion := "2.13.5"

val AkkaVersion = "2.6.13"
val ScalaTestVersion = "3.2.5"

libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test)