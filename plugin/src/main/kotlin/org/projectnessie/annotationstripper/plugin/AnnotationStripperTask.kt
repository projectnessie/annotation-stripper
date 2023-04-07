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

import java.net.URLClassLoader
import java.util.regex.Pattern
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.projectnessie.annotationstripper.core.StripAnnotations

abstract class AnnotationStripperTask
@Inject
constructor(
  private val stripSetName: String,
  private val fileSystemOperations: FileSystemOperations,
  @OutputDirectory val outputDirectory: DirectoryProperty,
  @Input val annotationsToDrop: ListProperty<Pattern>,
  @Optional @Input val unmodifiedClassesForJavaVersion: Property<Int>
) : DefaultTask() {

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sourceSetCompileClasspath: Property<FileCollection>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sourceSetClassesDirs: Property<FileCollection>

  @TaskAction
  fun stripClasses() {
    fileSystemOperations.delete { delete(outputDirectory) }

    val classesDirs = sourceSetClassesDirs.get()
    val compileClasspath = sourceSetCompileClasspath.get()

    // Need a ClassLoader constructed from the compilation classpath plus the class output
    // directories of the source set. This class loader is used by asm's ClassWriter.
    val classPathUrls =
      (compileClasspath.elements.get().map { f -> f.asFile } + classesDirs)
        .map { f -> f.toURI().toURL() }
        .toTypedArray()
    val dependenciesClassLoader = URLClassLoader(classPathUrls)

    classesDirs.forEach {
      val target = outputDirectory.get().asFile
      logger.info("Stripping annotations for source set '${stripSetName}' from $it into $target")
      val builder =
        StripAnnotations.builder()
          .dependenciesClassLoader(dependenciesClassLoader)
          .annotationsToDrop(annotationsToDrop.get())
          .targetDir(target.toPath())
          .classesSourceDir(it.toPath())

      if (unmodifiedClassesForJavaVersion.isPresent)
        builder.unmodifiedClassesForJavaVersion(unmodifiedClassesForJavaVersion.get())

      builder.build().stripAnnotations()
    }
  }
}
