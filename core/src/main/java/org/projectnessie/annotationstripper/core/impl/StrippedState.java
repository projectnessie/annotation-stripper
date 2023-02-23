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

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.objectweb.asm.Type;

final class StrippedState {

  private final List<Pattern> annotationsToDrop;
  private final Map<String, Boolean> descriptorChecks = new HashMap<>();
  private final Set<Path> createdDirs = new HashSet<>();

  boolean stripped;

  StrippedState(List<Pattern> annotationsToDrop) {
    this.annotationsToDrop = annotationsToDrop;
  }

  boolean dropAnnotation(String descriptor) {
    boolean drop = descriptorChecks.computeIfAbsent(descriptor, this::checkDescriptor);
    if (drop) {
      stripped = true;
    }
    return drop;
  }

  private boolean checkDescriptor(String descriptor) {
    Type type = Type.getType(descriptor);
    String className = type.getClassName();
    return annotationsToDrop.stream().map(p -> p.matcher(className)).anyMatch(Matcher::matches);
  }

  public void createDirectory(Path directory) throws IOException {
    if (createdDirs.add(directory)) {
      createDirectories(directory);
    }
  }
}
