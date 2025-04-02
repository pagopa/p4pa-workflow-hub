import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.*

plugins {
  java
  id("org.springframework.boot") version "3.4.3"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "6.0.1.5171"
  id("com.github.ben-manes.versions") version "0.51.0"
  id("org.openapi.generator") version "7.10.0"
  id("org.ajoberstar.grgit") version "5.3.0"
  id("com.gorylenko.gradle-git-properties") version "2.5.0"
}

group = "it.gov.pagopa.payhub"
version = "0.0.1"
description = "p4pa-workflow-hub"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/pagopa/p4pa-payhub-activities")
    credentials {
      username = "public"
      password = System.getenv("GITHUB_TOKEN")
    }
  }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
  }
}

val springDocOpenApiVersion = "2.8.5"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.4.3"
val bouncycastleVersion = "1.80"
val mapStructVersion = "1.6.3"
val temporalVersion = "1.27.1"
val protobufJavaVersion = "3.25.5"
val guavaVersion = "33.4.0-jre"
val postgresJdbcVersion = "42.7.5"
val podamVersion = "8.0.2.RELEASE"

val activitiesVersion = "1.91.1"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
    exclude(group = "org.glassfish.jaxb", module = "jaxb-core")
  }
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.mapstruct:mapstruct:$mapStructVersion")
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")
  implementation("org.postgresql:postgresql:$postgresJdbcVersion")
  implementation("it.gov.pagopa.payhub:p4pa-payhub-activities:$activitiesVersion") {
    exclude(group = "org.glassfish.jaxb", module = "jaxb-core")
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.guava", module = "guava")
  }
  // Temporal
  implementation("io.temporal:temporal-spring-boot-starter:$temporalVersion") {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.guava", module = "guava")
  }
  implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
  implementation("com.google.guava:guava:$guavaVersion")

  compileOnly("org.projectlombok:lombok")

  /**
   * Mapstruct
   * https://mapstruct.org/
   * mapstruct dependencies must always be placed after the lombok dependency
   * or the generated mappers will return an empty object
   **/
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.mapstruct:mapstruct-processor:$mapStructVersion")
  testAnnotationProcessor("org.projectlombok:lombok")
  testAnnotationProcessor("org.mapstruct:mapstruct-processor:$mapStructVersion")

  //	Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
  testImplementation("io.temporal:temporal-testing")
  testImplementation("com.h2database:h2")
  testImplementation("uk.co.jemos.podam:podam:$podamVersion")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}
tasks {
  test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
  }
}

val projectInfo = mapOf(
  "artifactId" to project.name,
  "version" to project.version
)

tasks {
  val processResources by getting(ProcessResources::class) {
    filesMatching("**/application.yml") {
      expand(projectInfo)
    }
  }
}

configurations {
  compileClasspath {
    resolutionStrategy.activateDependencyLocking()
  }
}

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerate",
    "openApiGenerateORGANIZATION"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  buildInfo()
  mainClass.value("it.gov.pagopa.pu.workflow.WorkflowApplication")
}

openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-workflow-hub.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.workflow.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.workflow.dto.generated")
  typeMappings.set(mapOf(
    "DebtPositionDTO" to "it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO",
    "IngestionFlowFileType" to "it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile.IngestionFlowFileTypeEnum",
    "WfExecutionConfig" to "it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig"
  ))
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "useBeanValidation" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Builder"
  ))
}

var targetEnv = when (Objects.requireNonNullElse(System.getProperty("targetBranch"), grgit.branch.current().name)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

tasks.register<GenerateTask>("openApiGenerateORGANIZATION") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.organization.generated")
  apiPackage.set("it.gov.pagopa.pu.organization.client.generated")
  modelPackage.set("it.gov.pagopa.pu.organization.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
