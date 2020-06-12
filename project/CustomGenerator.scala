import java.util

import io.swagger.v3.oas.models.OpenAPI
import org.apache.commons.lang3.StringUtils
import org.openapitools.codegen.api.TemplatingEngineAdapter
import org.openapitools.codegen.{CliOption, ClientOptInput, SupportingFile, TemplatingEngineLoader}
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.languages.StaticHtml2Generator

/**
 * Custom generator demonstrates how one might extend an existing generator to add functionality
 * @param fullTemplatePath The full path to custom templates. Required because it's basically impossible to add resources to meta-build in SBT.
 */
class CustomGenerator(fullTemplatePath: String) extends StaticHtml2Generator {
  embeddedTemplateDir = "customHtmlDocs2"
  templateDir = fullTemplatePath
  supportingFiles.add(new SupportingFile("humans.mustache", "", "humans.txt"))
  cliOptions.add(new CliOption(CustomGenerator.gaKey, "Your GA identifier to be injected into the page."))

  override def getName = "custom-html2"

  override def processOpts(): Unit = {
    super.processOpts()
    var gaIdentifier = additionalProperties.getOrDefault(CustomGenerator.gaKey, "XXXXX-X").toString
    if (!gaIdentifier.startsWith("GA-")) gaIdentifier = "GA-" + gaIdentifier
    additionalProperties.put(CustomGenerator.gaKey, gaIdentifier)
  }
}

object CustomGenerator {
  val gaKey = "googleAnalytics"

  /**
   * A helper function which mimics [[CodegenConfigurator#toClientOptInput]].
   * This is required because OpenAPI generator constructs generators using SPI, meaning one would need a META-INF/services file
   * which loads via classpath. Unfortunately, SBT makes it difficult (if not impossible) to add resources under meta-build to the
   * classpath.
   *
   * @param config Your custom config
   * @param configurator The OpenAPI Generator CodegenConfigurator holding all parsed config options
   * @return A [[ClientOptInput]] whic combines openapi doc + config for passing to the DefaultGenerator instance
   */
  def fillFromContext(config: CustomGenerator, configurator: CodegenConfigurator): ClientOptInput = {
    val options = configurator.toContext

    val workflowSettings = options.getWorkflowSettings
    val generatorSettings = options.getGeneratorSettings

    if (StringUtils.isNotEmpty(generatorSettings.getLibrary)) config.setLibrary(generatorSettings.getLibrary)
    val templatingEngine: TemplatingEngineAdapter = TemplatingEngineLoader.byIdentifier(workflowSettings.getTemplatingEngineName)

    config.setInputSpec(workflowSettings.getInputSpec)
    config.setOutputDir(workflowSettings.getOutputDir)
    config.setSkipOverwrite(workflowSettings.isSkipOverwrite)
    config.setIgnoreFilePathOverride(workflowSettings.getIgnoreFileOverride)
    config.setRemoveOperationIdPrefix(workflowSettings.isRemoveOperationIdPrefix)
    config.setEnablePostProcessFile(workflowSettings.isEnablePostProcessFile)
    config.setEnableMinimalUpdate(workflowSettings.isEnableMinimalUpdate)
    config.setStrictSpecBehavior(workflowSettings.isStrictSpecBehavior)
    config.setTemplatingEngine(templatingEngine)
    config.instantiationTypes().putAll(generatorSettings.getInstantiationTypes)
    config.typeMapping().putAll(generatorSettings.getTypeMappings)
    config.importMapping().putAll(generatorSettings.getImportMappings)
    config.languageSpecificPrimitives().addAll(generatorSettings.getLanguageSpecificPrimitives)
    config.reservedWordsMappings().putAll(generatorSettings.getReservedWordMappings)
    config.additionalProperties().putAll(generatorSettings.getAdditionalProperties)

    val serverVariables: util.Map[String, String] = generatorSettings.getServerVariables
    if (!serverVariables.isEmpty) {
      config.serverVariableOverrides.putAll(serverVariables)
    }

    val templateDir: String = workflowSettings.getTemplateDir
    if (templateDir != null) config.additionalProperties().put("templateDir", workflowSettings.getTemplateDir)

    val opts: ClientOptInput = (new ClientOptInput).config(config)
    opts.openAPI(options.getSpecDocument.asInstanceOf[OpenAPI])
  }
}