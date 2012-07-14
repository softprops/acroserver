name := "acro-server"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-netty-websockets" % "0.6.3",
  "net.databinder" %% "unfiltered-spec" % "0.6.3" % "test",
  "me.lessis" %% "tubesocks" % "0.1.0" % "test",
  "io.netty" % "netty" % "3.4.4.Final"
)

seq(assemblySettings:_*)
