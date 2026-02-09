import com.github.jk1.license.filter.*
import com.github.jk1.license.render.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.*

plugins {
  java
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "7.2.1.6560"
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.openapi.generator") version "7.17.0"
  id("org.ajoberstar.grgit") version "5.3.2"
  id("com.gorylenko.gradle-git-properties") version "2.5.4"
  id("com.github.jk1.dependency-license-report") version "3.0.1"
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
  compileClasspath {
    resolutionStrategy.activateDependencyLocking()
  }
}

licenseReport {
  renderers =
    arrayOf(XmlReportRenderer("third-party-libs.xml", "Back-End Libraries"))
  outputDir = "$projectDir/dependency-licenses"
  filters = arrayOf(SpdxLicenseBundleNormalizer())
}
tasks.classes {
  finalizedBy(tasks.generateLicenseReport)
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

val springDocOpenApiVersion = "3.0.0"
val openApiToolsVersion = "0.2.8"
val springWolfAsyncApiVersion = "1.20.0"
val micrometerVersion = "1.6.1"
val otelVersion = "1.57.0"
val bouncycastleVersion = "1.83"
val mapStructVersion = "1.6.3"
val temporalVersion = "1.32.1"
val protobufJavaVersion = "4.33.2"
val grpcBomVersion = "1.77.0"
val guavaVersion = "33.5.0-jre"
val postgresJdbcVersion = "42.7.8"
val podamVersion = "8.0.2.RELEASE"
val caffeineVersion = "3.2.3"
val commonsLang3Version = "3.20.0"
val lz4JavaVersion = "1.10.1"
val springCloudDepsVersion = "2025.1.0"

val p4paActivitiesVersion = "1.174.0-SNAPSHOT"

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudDepsVersion")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
  implementation("org.springframework.boot:spring-boot-starter-restclient")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
    exclude(group = "org.glassfish.jaxb", module = "jaxb-core")
  }
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("at.yawk.lz4:lz4-java:$lz4JavaVersion")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion") {
    exclude(group = "org.apache.commons", module = "commons-lang3")
  }
  implementation("org.apache.commons:commons-lang3:${commonsLang3Version}")
  implementation("io.github.springwolf:springwolf-kafka:$springWolfAsyncApiVersion") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("io.github.springwolf:springwolf-ui:$springWolfAsyncApiVersion")
  implementation("io.github.springwolf:springwolf-cloud-stream:$springWolfAsyncApiVersion")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.mapstruct:mapstruct:$mapStructVersion")
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")
  implementation("org.postgresql:postgresql:$postgresJdbcVersion")
  implementation("it.gov.pagopa.payhub:p4pa-payhub-activities:$p4paActivitiesVersion") {
    exclude(group = "org.glassfish.jaxb", module = "jaxb-core")
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-java-util")
    exclude(group = "com.google.guava", module = "guava")
  }
  // Temporal
  implementation("io.temporal:temporal-spring-boot-starter:$temporalVersion") {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
    exclude(group = "com.google.protobuf", module = "protobuf-java-util")
    exclude(group = "io.grpc", module = "grpc-bom")
    exclude(group = "com.google.guava", module = "guava")
  }
  implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")
  implementation("com.google.protobuf:protobuf-java-util:${protobufJavaVersion}")
  implementation(platform("io.grpc:grpc-bom:${grpcBomVersion}"))
  implementation("com.google.guava:guava:$guavaVersion")
  implementation("io.opentelemetry:opentelemetry-opentracing-shim:${otelVersion}")

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
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-security-test")
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
    testLogging.events = setOf(TestLogEvent.FAILED)
    testLogging.exceptionFormat = TestExceptionFormat.FULL
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

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerate",
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
  typeMappings.set(
    mapOf(
      "DebtPositionDTO" to "it.gov.pagopa.pu.debtposition.dto.generated.DebtPositionDTO",
      "IngestionFlowFileType" to "it.gov.pagopa.pu.processexecutions.dto.generated.IngestionFlowFile.IngestionFlowFileTypeEnum",
      "ExportFileType" to "it.gov.pagopa.pu.processexecutions.dto.generated.ExportFile.ExportFileTypeEnum",
      "WfExecutionConfig" to "it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.WfExecutionConfig",
      "FineWfExecutionConfig" to "it.gov.pagopa.payhub.activities.dto.debtposition.syncwfconfig.FineWfExecutionConfig",
      "WorkflowTypeOrg" to "it.gov.pagopa.pu.workflow.model.WorkflowTypeOrg",
      "ScheduleEnum" to "it.gov.pagopa.pu.workflow.enums.ScheduleEnum",
      "WorkflowExecutionStatus" to "io.temporal.api.enums.v1.WorkflowExecutionStatus"
    )
  )
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "requestMappingMode" to "api_interface",
      "useSpringBoot3" to "true",
      "interfaceOnly" to "true",
      "useTags" to "true",
      "useBeanValidation" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "additionalModelTypeAnnotations" to "@lombok.Builder"
    )
  )
}

var targetEnv = when (Objects.requireNonNullElse(
  System.getProperty("targetBranch"),
  grgit.branch.current().name
)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
