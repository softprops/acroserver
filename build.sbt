name := "acro-server"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-server" % "0.6.2" % "test",
  "io.netty" % "netty" % "3.4.4.Final"
)

seq(assemblySettings:_*)
