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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.tasks.Jar

@Suppress("unused")
open class AnnotationStripperPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit =
    project.run {
      val ext = extensions.create("annotationStripper", AnnotationStripperExtension::class.java)

      ext.stripSets.whenObjectAdded {
        annotationsToDrop.convention(emptyList())

        val sourceSet = sourceSet(project)

        val jar = project.tasks.named(sourceSet.jarTaskName, Jar::class.java)
        val classes = project.tasks.named(sourceSet.classesTaskName)

        val stripSet = this
        val stripTaskName = "strip${if (name == "main") "" else name.capitalized()}Annotations"

        val output = project.objects.directoryProperty()
        output.set(buildDir.resolve("classes/annotationStripped").resolve(sourceSet.name))

        val stripTask =
          tasks.register(
            stripTaskName,
            AnnotationStripperTask::class.java,
            stripSet.name,
            output,
            stripSet.annotationsToDrop,
            stripSet.unmodifiedClassesForJavaVersion
          )
        stripTask.configure {
          description =
            "Strip annotations from the compiled class files of the '${sourceSet.name}' source set."
          dependsOn(classes)
          sourceSetClassesDirs.convention(sourceSet.output.classesDirs)
          sourceSetCompileClasspath.convention(sourceSet.compileClasspath)
          unmodifiedClassesForJavaVersion.convention(stripSet.unmodifiedClassesForJavaVersion)
        }

        jar.configure {
          dependsOn(stripTask)

          from(output)

          manifest { attributes["Multi-Release"] = true }

          // Some "exclude dance" here
          exclude { fte ->
            if (fte.isDirectory) {
              false
            } else {
              // TODO memoize stripTask.get().outputDirectory.get().asFile
              val stripped = output.get().asFile
              if (fte.file.startsWith(stripped)) {
                false
              } else {
                stripped.resolve(fte.relativePath.pathString).exists()
              }
            }
          }
        }
      }
    }
}
