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
  `maven-publish`
  id("com.gradle.plugin-publish")
  id("com.diffplug.spotless")
  `annotation-stripper-conventions`
}

if (project.hasProperty("release")) {
  apply<SigningPlugin>()
}

description = "Strips annotations from class files"

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

tasks.withType(Test::class.java).configureEach {
  jvmArgs(
    "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
    "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
    "--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED"
  )
}

publishing {
  publications {
    withType(MavenPublication::class.java).configureEach {
      pom {
        name.set("Application Stripper Gradle Plugin")
        description.set(project.description)

        withXml {
          val projectNode = asNode()

          val parentNode = projectNode.appendNode("parent")
          parentNode.appendNode("groupId", parent!!.group)
          parentNode.appendNode("artifactId", parent!!.name)
          parentNode.appendNode("version", parent!!.version)
        }
      }
    }
  }
}
