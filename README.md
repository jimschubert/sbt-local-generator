# sbt-local-generator

You may not necessarily need the sbt-openapi-generator plugin to add code generation to your app. You may prefer to:

* Include your custom code generator in the same repository as your application code
* Have full control over configuration parsing and generation
* Create your own configuration tasks and settings
* Reduce the plugin lifecycle in your application

This example shows how to add a custom generator and custom templates to your Scala project without pulling in the SBT Plugin.

This _does_ still require openapi-generator. However, it demonstrates how to solve an issue where OpenAPI Generator expects to
be able to load your custom generator via SPI (via classpath). SBT makes this very difficult, and there's been an
[issue open since 2017](https://github.com/sbt/sbt/issues/3498) regarding this.

We need to circumvent that funky SBT classpath behavior.

This example shows how to:

* Load a YAML document from the repository root (following a spec-driven design approach)
* Reference openapi-generator as a build-time dependency (see `project/build.sbt`) 
* Define a custom generator in the meta-build project at `project/CustomGenerator`
  - This custom generator adds a Google Analytics key support to the static html generator built-in to OpenAPI Generator
* Load your custom generator without the ability or need to modify classpath of the meta-build project
* Load your custom templates using SBT's path resolution via the `baseDirectory` setting
* Define a custom task called `codegen`, which could be chained to other tasks. Please refer to SBT's docs.

## Build

To build, run:

```
sbt codegen
```

This creates a `docs` directory in your project root.

The example codegen sets a Google Analytics key in the generated html of `GA-123456789`. Verify generation like so:

```shell
$ grep GA-123456789 docs/index.html
      ga('create','GA-123456789','auto');ga('send','pageview');
```
