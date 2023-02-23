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

import java.util.regex.Pattern
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

interface AnnotationStripperExtension {
  val stripSets: NamedDomainObjectContainer<AnnotationStripSet>

  /** Register annotation stripping for the `main` source set. */
  @Suppress("unused")
  fun registerDefault(): NamedDomainObjectProvider<AnnotationStripSet> {
    return stripSets.register("main")
  }
}

abstract class AnnotationStripSet : Named {
  abstract val annotationsToDrop: ListProperty<Pattern>

  abstract val unmodifiedClassesForJavaVersion: Property<Int>

  internal fun sourceSet(project: Project): SourceSet =
    project.extensions.getByType(SourceSetContainer::class.java).getByName(name)

  fun annotationsToDrop(vararg regex: Regex) {
    annotationsToDrop.addAll(regex.map { r -> r.toPattern() })
  }

  fun annotationsToDrop(vararg regex: String) {
    annotationsToDrop.addAll(regex.map { r -> Pattern.compile(r) })
  }

  fun annotationsToDrop(vararg regex: Pattern) {
    annotationsToDrop.addAll(regex.toList())
  }
}
