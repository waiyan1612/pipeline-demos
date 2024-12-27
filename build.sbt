ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val sparkVersion = "3.5.1"
lazy val sparkDependencies = Seq(
  "spark-core",
  "spark-sql",
).map(artifactId => "org.apache.spark" %% artifactId % sparkVersion % "provided")

lazy val flinkVersion = "2.0-preview1"
lazy val flinkDependencies = Seq(
  "flink-clients",
  // Dependencies for CSV
  "flink-connector-files",
  "flink-csv",
).map(artifactId => "org.apache.flink" % artifactId % flinkVersion % "provided")

ThisBuild / scalaVersion := "2.13.15"

lazy val `demo-data` = (project in file("demo-data"))

lazy val spark = (project in file("spark"))
  .settings(libraryDependencies ++= sparkDependencies)
  .dependsOn(`demo-data`)

lazy val flink = (project in file("flink"))
  .settings(libraryDependencies ++= flinkDependencies)
  .dependsOn(`demo-data`)

lazy val root = (project in file("."))
  .settings(name := "pipeline-demos")
  .dependsOn(spark)
  .dependsOn(flink)
