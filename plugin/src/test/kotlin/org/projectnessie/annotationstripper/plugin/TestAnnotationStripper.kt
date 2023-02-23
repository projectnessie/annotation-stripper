/*
 * Copyright (C) 2023 The Authors of this project
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

package org.projectnessie.annotationstripper.plugin

import java.net.URL
import java.nio.file.Files
import java.nio.file.Files.copy
import java.nio.file.Files.createDirectories
import java.nio.file.Files.walk
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.zip.ZipFile
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir

@ExtendWith(SoftAssertionsExtension::class)
class TestAnnotationStripper {
  @TempDir var projectDir: Path? = null

  @InjectSoftAssertions var soft: SoftAssertions? = null

  @Test
  fun checkPlugin() {
    copyProject(TestAnnotationStripper::class.java.classLoader.getResource("test-cases/jakarta")!!)

    createRunner("jar").build()

    val buildDir = projectDir!!.resolve("build")
    val strippedClasses = buildDir.resolve("classes/annotationStripped/main")
    val filesInDir =
      walk(strippedClasses).use { pathStream ->
        pathStream
          .filter(Files::isRegularFile)
          .map { p -> strippedClasses.relativize(p).toString() }
          .collect(Collectors.toList())
      }

    val filesInJar =
      ZipFile(buildDir.resolve("libs/testcase-jakarta-0.0-SNAPSHOT.jar").toFile()).stream().use {
        it.filter { e -> !e.isDirectory }.map { e -> e.name }.collect(Collectors.toList())
      }

    soft!!
      .assertThat(filesInJar)
      .containsAll(filesInDir)
      .contains("foo/foo.txt")
      .contains("testcase/jakarta/Unannotated.class")
      .contains("testcase/Something.class")
    soft!!
      .assertThat(filesInDir)
      .isNotEmpty
      .doesNotContain("foo/foo.txt")
      .doesNotContain("testcase/jakarta/Unannotated.class")
      .doesNotContain("testcase/Something.class")
  }

  private fun createRunner(task: String): GradleRunner =
    GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir!!.toFile())
      .withArguments("--build-cache", "--info", "--stacktrace", task)
      .withDebug(true)
      .forwardOutput()

  private fun copyProject(resource: URL) {
    val src = Paths.get(resource.toURI())
    walk(src).use { files ->
      files.forEach { f ->
        val relative = src.relativize(f)
        val target = projectDir!!.resolve(relative)
        if (Files.isDirectory(f)) {
          createDirectories(target)
        } else if (Files.isRegularFile(f)) {
          copy(f, target)
        }
      }
    }
  }
}
