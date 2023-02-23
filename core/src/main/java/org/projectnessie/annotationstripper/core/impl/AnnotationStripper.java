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
package org.projectnessie.annotationstripper.core.impl;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.write;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ASM9;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.projectnessie.annotationstripper.core.StripAnnotations;

/** Internal implementation class, use {@link StripAnnotations#stripAnnotations()}. */
public class AnnotationStripper {

  static final int API = ASM9;

  private final StripAnnotations cfg;

  public AnnotationStripper(StripAnnotations cfg) {
    this.cfg = cfg;
  }

  public void stripAnnotations() throws IOException {
    createDirectories(cfg.targetDir());

    StrippedState state = new StrippedState(cfg.annotationsToDrop());

    Path unmodifiedTarget =
        cfg.unmodifiedClassesForJavaVersion().isPresent()
            ? cfg.targetDir()
                .resolve("META-INF/versions")
                .resolve(Integer.toString(cfg.unmodifiedClassesForJavaVersion().getAsInt()))
            : null;

    try (Stream<Path> files = walk(cfg.classesSourceDir())) {
      files.forEach(p -> processPath(state, p, unmodifiedTarget));
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private void processPath(StrippedState state, Path src, Path unmodifiedTarget) {
    try {
      Path relative = cfg.classesSourceDir().relativize(src);
      Path dest = cfg.targetDir().resolve(relative);

      if (isDirectory(src)) {
        if (cfg.copyUnmodifiedClasses()) {
          createDirectories(dest);
        }
      } else if (isRegularFile(src)) {
        String fileName = src.getFileName().toString();
        if (fileName.endsWith(".class")) {
          processClass(
              state,
              src,
              dest,
              () -> unmodifiedTarget != null ? unmodifiedTarget.resolve(relative) : null);
        } else {
          if (cfg.copyUnmodifiedClasses()) {
            copy(src, dest);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (Exception e) {
      throw new RuntimeException("Failure handling " + src, e);
    }
  }

  private void processClass(StrippedState state, Path src, Path dest, Supplier<Path> unmodifiedDest)
      throws IOException {
    ClassWriter classWriter =
        new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES) {
          @Override
          protected ClassLoader getClassLoader() {
            return cfg.dependenciesClassLoader()
                .orElse(Thread.currentThread().getContextClassLoader());
          }
        };

    try (InputStream input = Files.newInputStream(src)) {
      state.stripped = false;
      ClassVisitor classVisitor = new AnnStrippingClassVisitor(state, classWriter);
      ClassReader classReader = new ClassReader(input);
      classReader.accept(classVisitor, 0);
    }

    if (state.stripped) {
      state.createDirectory(dest.getParent());
      write(dest, classWriter.toByteArray());
      Path unmodified = unmodifiedDest.get();
      if (unmodified != null) {
        state.createDirectory(unmodified.getParent());
        copy(src, unmodified);
      }
    } else {
      if (cfg.copyUnmodifiedClasses()) {
        copy(src, dest);
      }
    }
  }
}
