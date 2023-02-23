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
  `kotlin-dsl`
  id("com.gradle.plugin-publish")
  id("com.diffplug.spotless")
  `annotation-stripper-conventions`
}

if (project.hasProperty("release")) {
  apply<SigningPlugin>()
}

dependencies {
  implementation(project(":annotation-stripper-core"))

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.junit.testing)
  testRuntimeOnly(libs.junit.jupiter.engine)

  testImplementation(gradleTestKit())
}

gradlePlugin {
  plugins {
    register("annotationStripper") {
      id = "org.projectnessie.annotation-stripper"
      implementationClass = "org.projectnessie.annotationstripper.plugin.AnnotationStripperPlugin"
      displayName = "Java annotation stripper"
      description = "Strips the configured annotations from built class files"
      tags.addAll("java", "annotations", "jakarta")
    }
  }
  website.set("https://projectnessie.org")
  vcsUrl.set("https://github.com/projectnessie/annotation-stripper")
}

kotlin { jvmToolchain(11) }

tasks.named("pluginUnderTestMetadata") { dependsOn(tasks.named("processJandexIndex")) }
