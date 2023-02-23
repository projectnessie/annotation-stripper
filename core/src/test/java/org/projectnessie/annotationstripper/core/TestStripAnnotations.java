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

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;

import java.nio.file.Path;
import java.util.regex.Pattern;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(SoftAssertionsExtension.class)
public class TestStripAnnotations {

  @InjectSoftAssertions protected SoftAssertions soft;

  @Test
  void constraintPatterns(@TempDir Path sourceDir, @TempDir Path targetDir) {
    soft.assertThatIllegalStateException()
        .isThrownBy(
            () ->
                StripAnnotations.builder().classesSourceDir(sourceDir).targetDir(targetDir).build())
        .withMessage("Must provide at least one pattern defining the annotations to drop");
  }

  @Test
  void sameSourceAndTarget(@TempDir Path sourceDir) {
    soft.assertThatIllegalStateException()
        .isThrownBy(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(sourceDir)
                    .targetDir(sourceDir)
                    .addAnnotationsToDrop(Pattern.compile(".*"))
                    .build())
        .withMessage("Source classes directory and target directory must not be the same");
  }

  @Test
  void sourceDirNotExists(@TempDir Path sourceDir, @TempDir Path targetDir) throws Exception {
    delete(sourceDir);
    delete(targetDir);
    soft.assertThatIllegalStateException()
        .isThrownBy(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(sourceDir)
                    .targetDir(targetDir)
                    .addAnnotationsToDrop(Pattern.compile(".*"))
                    .build())
        .withMessage("Source classes directory must exist");
  }

  @Test
  void targetDirIsFile(@TempDir Path sourceDir, @TempDir Path targetDir) throws Exception {
    delete(targetDir);
    createFile(targetDir);
    soft.assertThatIllegalStateException()
        .isThrownBy(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(sourceDir)
                    .targetDir(targetDir)
                    .addAnnotationsToDrop(Pattern.compile(".*"))
                    .build())
        .withMessage(
            "Target classes parameter must either point to an existing directory or not exist");
  }

  @Test
  void goodCases(@TempDir Path sourceDir, @TempDir Path targetDir) throws Exception {
    soft.assertThatCode(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(sourceDir)
                    .targetDir(targetDir)
                    .addAnnotationsToDrop(Pattern.compile(".*"))
                    .build())
        .doesNotThrowAnyException();

    delete(targetDir);

    soft.assertThatCode(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(sourceDir)
                    .targetDir(targetDir)
                    .addAnnotationsToDrop(Pattern.compile(".*"))
                    .build())
        .doesNotThrowAnyException();
  }
}
