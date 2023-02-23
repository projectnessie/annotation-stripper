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
package org.projectnessie.annotationstripper.core;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import org.immutables.value.Value;
import org.projectnessie.annotationstripper.core.impl.AnnotationStripper;

@Value.Immutable
public interface StripAnnotations {

  Path classesSourceDir();

  Path targetDir();

  List<Pattern> annotationsToDrop();

  Optional<ClassLoader> dependenciesClassLoader();

  /**
   * Whether to place unmodified classes in {@link #targetDir()} as well, default is {@code false}.
   */
  @Value.Default
  default boolean copyUnmodifiedClasses() {
    return false;
  }

  /**
   * If annotations had been stripped from a class file and this value is set to a Java release set
   * to 9 or higher, the unmodified source class files will be available via a multi-release jar
   * directory structure in {@link #targetDir()} for the configured Java release.
   *
   * <p>If this is value is not configured, the original source files will not be placed in the
   * {@link #targetDir()}.
   *
   * <p>Classes will also be copied into the multi-release jar structure, if {@link
   * #copyUnmodifiedClasses()} is {@code false}.
   */
  OptionalInt unmodifiedClassesForJavaVersion();

  default void stripAnnotations() throws IOException {
    new AnnotationStripper(this).stripAnnotations();
  }

  static Builder builder() {
    return ImmutableStripAnnotations.builder();
  }

  interface Builder {

    Builder from(StripAnnotations instance);

    Builder classesSourceDir(Path classesSourceDir);

    Builder targetDir(Path targetDir);

    Builder addAnnotationsToDrop(Pattern element);

    Builder addAnnotationsToDrop(Pattern... element);

    Builder addAllAnnotationsToDrop(Iterable<? extends Pattern> elements);

    Builder annotationsToDrop(Iterable<? extends Pattern> elements);

    Builder copyUnmodifiedClasses(boolean copyUnmodifiedClasses);

    Builder unmodifiedClassesForJavaVersion(int unmodifiedClassesForJavaVersion);

    Builder unmodifiedClassesForJavaVersion(OptionalInt unmodifiedClassesForJavaVersion);

    Builder dependenciesClassLoader(ClassLoader dependenciesClassLoader);

    Builder dependenciesClassLoader(Optional<? extends ClassLoader> dependenciesClassLoader);

    StripAnnotations build();
  }

  @Value.Check
  default void check() {
    checkState(
        !annotationsToDrop().isEmpty(),
        "Must provide at least one pattern defining the annotations to drop");
    checkState(
        !targetDir().equals(classesSourceDir()),
        "Source classes directory and target directory must not be the same");
    checkState(Files.isDirectory(classesSourceDir()), "Source classes directory must exist");
    checkState(
        Files.isDirectory(targetDir()) || !Files.exists(targetDir()),
        "Target classes parameter must either point to an existing directory or not exist");
  }
}
