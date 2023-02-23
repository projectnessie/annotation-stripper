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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

final class AnnStrippingMethodVisitor extends MethodVisitor {

  private final StrippedState state;

  AnnStrippingMethodVisitor(StrippedState state, MethodVisitor methodVisitor) {
    super(AnnotationStripper.API, methodVisitor);
    this.state = state;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    if (state.dropAnnotation(descriptor)) {
      return null;
    }
    return super.visitAnnotation(descriptor, visible);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(
      int typeRef, TypePath typePath, String descriptor, boolean visible) {
    if (state.dropAnnotation(descriptor)) {
      return null;
    }
    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(
      int parameter, String descriptor, boolean visible) {
    if (state.dropAnnotation(descriptor)) {
      return null;
    }
    return super.visitParameterAnnotation(parameter, descriptor, visible);
  }
}
