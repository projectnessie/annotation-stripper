/*
 * Copyright (C) 2021 The Authors of this project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  `java-library`
  `maven-publish`
  id("com.diffplug.spotless")
  `annotation-stripper-conventions`
  id("org.projectnessie.buildsupport.publishing")
}

dependencies {
  implementation(libs.asm)
  implementation(libs.asm.commons)
  implementation(libs.guava)

  compileOnly(libs.immutables.builder)
  compileOnly(libs.immutables.value.annotations)
  annotationProcessor(libs.immutables.value.processor)

  testImplementation(libs.asm.tree)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.junit.testing)
  testRuntimeOnly(libs.junit.jupiter.engine)
}

val testCaseJakarta by
  sourceSets.registering {
    java {
      srcDir("testCase/jakarta/src/java")
      destinationDirectory.set(project.buildDir.resolve("testCases/jakarta/classes"))
    }
  }

val compileTestCaseJakarta by
  tasks.registering(JavaCompile::class) {
    source = testCaseJakarta.get().java.sourceDirectories.asFileTree
    destinationDirectory.set(testCaseJakarta.get().java.destinationDirectory)
    classpath = configurations.getByName("testCaseJakartaCompileClasspath")
  }

dependencies {
  add("testCaseJakartaImplementation", "org.projectnessie:nessie-model:0.50.0")

  add("testCaseJakartaCompileOnly", "javax.ws.rs:javax.ws.rs-api:2.1.1")
  add("testCaseJakartaCompileOnly", "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
  add("testCaseJakartaCompileOnly", "javax.validation:validation-api:2.0.1.Final")
  add("testCaseJakartaCompileOnly", "jakarta.validation:jakarta.validation-api:3.0.2")
}

tasks.named<Test>("test") {
  dependsOn(compileTestCaseJakarta)
  systemProperty(
    "testCase.jakarta.classes",
    testCaseJakarta.get().java.destinationDirectory.asFile.get().relativeTo(projectDir)
  )
}

tasks.named("jandexTestCaseJakarta") { dependsOn(tasks.named("compileTestCaseJakarta")) }

tasks.named("checkstyleTestCaseJakarta") { dependsOn(tasks.named("compileTestCaseJakarta")) }
