plugins {
  java
  id("org.springframework.boot") version "3.4.1"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "6.0.1.5171"
  id("com.github.ben-manes.versions") version "0.51.0"
  id("org.openapi.generator") version "7.10.0"
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

val springDocOpenApiVersion = "2.7.0"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.4.1"
val temporalVersion = "1.27.0"
val protobufJavaVersion = "3.25.5"
val bouncycastleVersion = "1.79"
val activitiesVersion = "1.31.0"
val mapStructVersion = "1.6.3"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")

  //security
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")

  implementation("it.gov.pagopa.payhub:p4pa-payhub-activities:$activitiesVersion") {
    exclude(group = "org.glassfish.jaxb", module = "jaxb-core")
  }

  implementation("io.temporal:temporal-spring-boot-starter:$temporalVersion"){
    exclude(group = "com.google.protobuf", module = "protobuf-java")
  }

  // updated for security reason
  implementation("com.google.protobuf:protobuf-java:$protobufJavaVersion")

  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")

  /**
   * Mapstruct
   * https://mapstruct.org/
   * mapstruct dependencies must always be placed after the lombok dependency
   * or the generated mappers will return an empty object
   **/
  implementation("org.mapstruct:mapstruct:$mapStructVersion")
  annotationProcessor("org.mapstruct:mapstruct-processor:$mapStructVersion")

  //	Testing
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
  testImplementation("io.temporal:temporal-testing")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
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
  dependsOn("openApiGenerate")
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  mainClass.value("it.gov.pagopa.pu.workflow.WorkflowApplication")
}

openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-workflow-hub.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.workflow.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.workflow.dto.generated")
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "requestMappingMode" to "api_interface",
      "useSpringBoot3" to "true",
      "interfaceOnly" to "true",
      "useTags" to "true",
      "generateConstructorWithAllArgs" to "false",
      "generatedConstructorWithRequiredArgs" to "true",
      "additionalModelTypeAnnotations" to "@lombok.Data @lombok.Builder @lombok.AllArgsConstructor"
    )
  )
}

tasks.withType<Copy> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

configurations.all {
  resolutionStrategy {
    force("org.glassfish.jaxb:jaxb-core:4.0.5")
  }
}
