import org.openapitools.codegen.DefaultGenerator
import org.openapitools.codegen.config.CodegenConfigurator

scalaVersion := "2.13.1"
name := "sbt-local-generator"
organization := "us.jimschubert"
version := "1.0"

// The codegen task shows how to embed a custom generator in the same repository as your code, without modifying classpath
lazy val codegen = taskKey[Unit]("Execute codegen without modifying classpath or building a separate jar")
codegen := {
  val generator = new DefaultGenerator()
  // Load config inputs
  val configurator = CodegenConfigurator.fromFile((baseDirectory.value / "gen.yaml").getAbsolutePath)

  // Do your configurations here, or load them from your build settings.
  configurator.addAdditionalProperty(CustomGenerator.gaKey, "GA-123456789")

  // Construct your custom generator, passing in the absolute path to your custom templates
  val config = new CustomGenerator((baseDirectory.value / "project" / "customHtmlDocs2").getAbsolutePath)

  // HACK : for configurator to succeed on getting context, we need to have *some* name set.
  configurator.setGeneratorName(config.getName)

  // Call helper function which is custom for this embedded generator
  val opts = CustomGenerator.fillFromContext(config, configurator)
  generator.opts(opts).generate()
}
