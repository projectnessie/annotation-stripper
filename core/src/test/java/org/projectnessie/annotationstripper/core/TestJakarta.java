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

import static java.lang.System.getProperty;
import static java.nio.file.Files.newInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.objectweb.asm.Opcodes.ASM9;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

@ExtendWith(SoftAssertionsExtension.class)
public class TestJakarta {

  private static final Pattern JAKARTA_PATTERN = Pattern.compile("^jakarta[.].+");
  public static final String CLASS_ANNOTATION = "testcase.jakarta.PathAnnotationOnClass";
  public static final String METHOD_ANNOTATION = "testcase.jakarta.RestMethodAnnotation";
  public static final String PARAMETER_ANNOTATION =
      "testcase.jakarta.RestMethodParameterAnnotation";
  public static final String COMPLEX_CLASS = "testcase.jakarta.ComplexCase";
  public static final String COMPLEX_INNER_CLASS = "testcase.jakarta.ComplexCase$InnerClass";
  public static final String COMPLEX_INNER_INNER_CLASS =
      "testcase.jakarta.ComplexCase$InnerClass$InnerInnerClass";
  public static final String COMPLEX_INNER_STATIC_CLASS =
      "testcase.jakarta.ComplexCase$InnerStaticClass";
  public static final String COMPLEX_INNER_INNER_STATIC_CLASS =
      "testcase.jakarta.ComplexCase$InnerStaticClass$InnerInnerStaticClass";

  static Path jakartaClasses;

  @BeforeAll
  static void setup() {
    jakartaClasses = Paths.get(getProperty("testCase.jakarta.classes"));
    assertThat(jakartaClasses).isNotEmptyDirectory();
  }

  @InjectSoftAssertions protected SoftAssertions soft;

  @Test
  void copyUntouched(@TempDir Path targetDirectory) {
    soft.assertThatCode(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(jakartaClasses)
                    .targetDir(targetDirectory)
                    .addAnnotationsToDrop(JAKARTA_PATTERN)
                    .copyUnmodifiedClasses(true)
                    .build()
                    .stripAnnotations())
        .doesNotThrowAnyException();

    soft.assertThat(targetDirectory.resolve("testcase/jakarta/Unannotated.class"))
        .isRegularFile()
        .hasSameBinaryContentAs(jakartaClasses.resolve("testcase/jakarta/Unannotated.class"));
  }

  @Test
  void jakartaOnClass(@TempDir Path targetDirectory) throws Exception {
    soft.assertThatCode(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(jakartaClasses)
                    .targetDir(targetDirectory)
                    .addAnnotationsToDrop(JAKARTA_PATTERN)
                    .build()
                    .stripAnnotations())
        .doesNotThrowAnyException();

    soft.assertThat(targetDirectory.resolve("META-INF/versions")).doesNotExist();
    soft.assertThat(targetDirectory.resolve("testcase/jakarta/text-file.txt")).doesNotExist();
    soft.assertThat(targetDirectory.resolve("testcase/jakarta/Unannotated.class")).doesNotExist();

    verifyAnnotationOnClass(targetDirectory, CLASS_ANNOTATION);
    verifyAnnotationOnMethod(targetDirectory, METHOD_ANNOTATION);
    verifyAnnotationOnParameter(targetDirectory, PARAMETER_ANNOTATION);
    verifyAnnotationOnComplex(targetDirectory);
  }

  @Test
  void jakartaOnClassDuplicate(@TempDir Path targetDirectory) throws Exception {
    soft.assertThatCode(
            () ->
                StripAnnotations.builder()
                    .classesSourceDir(jakartaClasses)
                    .targetDir(targetDirectory)
                    .addAnnotationsToDrop(JAKARTA_PATTERN)
                    .unmodifiedClassesForJavaVersion(11)
                    .build()
                    .stripAnnotations())
        .doesNotThrowAnyException();

    Path mrDir = targetDirectory.resolve("META-INF/versions/11");

    soft.assertThat(classFile(mrDir, CLASS_ANNOTATION))
        .isRegularFile()
        .hasSameBinaryContentAs(classFile(jakartaClasses, CLASS_ANNOTATION));
    soft.assertThat(classFile(mrDir, METHOD_ANNOTATION))
        .isRegularFile()
        .hasSameBinaryContentAs(classFile(jakartaClasses, METHOD_ANNOTATION));
    soft.assertThat(classFile(mrDir, PARAMETER_ANNOTATION))
        .isRegularFile()
        .hasSameBinaryContentAs(classFile(jakartaClasses, PARAMETER_ANNOTATION));

    verifyAnnotationOnClass(targetDirectory, CLASS_ANNOTATION);
    verifyAnnotationOnMethod(targetDirectory, METHOD_ANNOTATION);
    verifyAnnotationOnParameter(targetDirectory, PARAMETER_ANNOTATION);
    verifyAnnotationOnComplex(targetDirectory);
  }

  private void verifyAnnotationOnComplex(Path targetDirectory) throws IOException {
    verifyAnnotationOnClass(targetDirectory, COMPLEX_CLASS);
    verifyAnnotationOnField(targetDirectory, COMPLEX_CLASS);
    verifyAnnotationOnMethod(targetDirectory, COMPLEX_CLASS);
    verifyAnnotationOnParameter(targetDirectory, COMPLEX_CLASS);

    verifyAnnotationOnClass(targetDirectory, COMPLEX_INNER_CLASS);
    verifyAnnotationOnField(targetDirectory, COMPLEX_INNER_CLASS);
    verifyAnnotationOnMethod(targetDirectory, COMPLEX_INNER_CLASS);
    verifyAnnotationOnParameter(targetDirectory, COMPLEX_INNER_CLASS);

    verifyAnnotationOnClass(targetDirectory, COMPLEX_INNER_INNER_CLASS);
    verifyAnnotationOnField(targetDirectory, COMPLEX_INNER_INNER_CLASS);
    verifyAnnotationOnMethod(targetDirectory, COMPLEX_INNER_INNER_CLASS);
    verifyAnnotationOnParameter(targetDirectory, COMPLEX_INNER_INNER_CLASS);

    verifyAnnotationOnClass(targetDirectory, COMPLEX_INNER_STATIC_CLASS);
    verifyAnnotationOnField(targetDirectory, COMPLEX_INNER_STATIC_CLASS);
    verifyAnnotationOnMethod(targetDirectory, COMPLEX_INNER_STATIC_CLASS);
    verifyAnnotationOnParameter(targetDirectory, COMPLEX_INNER_STATIC_CLASS);

    verifyAnnotationOnClass(targetDirectory, COMPLEX_INNER_INNER_STATIC_CLASS);
    verifyAnnotationOnField(targetDirectory, COMPLEX_INNER_INNER_STATIC_CLASS);
    verifyAnnotationOnMethod(targetDirectory, COMPLEX_INNER_INNER_STATIC_CLASS);
    verifyAnnotationOnParameter(targetDirectory, COMPLEX_INNER_INNER_STATIC_CLASS);
  }

  private void verifyAnnotationOnParameter(Path targetDirectory, String clazz) throws IOException {
    ClassNode sourceClass = loadIsolatedClass(jakartaClasses, clazz);
    ClassNode targetClass = loadIsolatedClass(targetDirectory, clazz);

    fooMethodParameterAnnotations(sourceClass)
        .extracting(an -> an.desc)
        .containsExactlyInAnyOrder("Ljavax/ws/rs/PathParam;", "Ljakarta/ws/rs/PathParam;");
    fooMethodParameterAnnotations(targetClass)
        .extracting(an -> an.desc)
        .containsExactly("Ljavax/ws/rs/PathParam;");
  }

  private void verifyAnnotationOnMethod(Path targetDirectory, String clazz) throws IOException {
    ClassNode sourceClass = loadIsolatedClass(jakartaClasses, clazz);
    ClassNode targetClass = loadIsolatedClass(targetDirectory, clazz);

    fooMethodAssert(sourceClass)
        .containsExactlyInAnyOrder("Ljavax/ws/rs/GET;", "Ljakarta/ws/rs/GET;");
    fooMethodAssert(targetClass).containsExactly("Ljavax/ws/rs/GET;");
  }

  private void verifyAnnotationOnField(Path targetDirectory, String clazz) throws IOException {
    ClassNode sourceClass = loadIsolatedClass(jakartaClasses, clazz);
    ClassNode targetClass = loadIsolatedClass(targetDirectory, clazz);

    beanParamAssert(sourceClass)
        .containsExactlyInAnyOrder("Ljavax/ws/rs/BeanParam;", "Ljakarta/ws/rs/BeanParam;");
    beanParamAssert(targetClass).containsExactlyInAnyOrder("Ljavax/ws/rs/BeanParam;");
  }

  private void verifyAnnotationOnClass(Path targetDirectory, String clazz) throws IOException {
    ClassNode sourceClass = loadIsolatedClass(jakartaClasses, clazz);
    ClassNode targetClass = loadIsolatedClass(targetDirectory, clazz);

    classAnnotations(sourceClass)
        .containsExactlyInAnyOrder("Ljavax/ws/rs/Path;", "Ljakarta/ws/rs/Path;");
    classAnnotations(targetClass).containsExactly("Ljavax/ws/rs/Path;");
  }

  private AbstractListAssert<?, List<? extends String>, String, ObjectAssert<String>>
      classAnnotations(ClassNode sourceClass) {
    return soft.assertThat(sourceClass.visibleAnnotations).extracting(an -> an.desc);
  }

  private ListAssert<AnnotationNode> fooMethodParameterAnnotations(ClassNode classNode) {
    return soft.assertThat(fooMethod(classNode))
        .get(type(MethodNode.class))
        .extracting(
            mn -> {
              for (int p = 0; p < mn.parameters.size(); p++) {
                if (mn.parameters.get(p).name.equals("meep")) {
                  return mn.visibleParameterAnnotations[p];
                }
              }
              return null;
            },
            list(AnnotationNode.class));
  }

  private AbstractListAssert<?, List<? extends String>, String, ObjectAssert<String>>
      fooMethodAssert(ClassNode classNode) {
    return soft.assertThat(fooMethod(classNode))
        .get(type(MethodNode.class))
        .extracting(mn -> mn.visibleAnnotations, list(AnnotationNode.class))
        .extracting(an -> an.desc);
  }

  private AbstractListAssert<?, List<? extends String>, String, ObjectAssert<String>>
      beanParamAssert(ClassNode classNode) {
    return soft.assertThat(
            classNode.fields.stream().filter(fn -> fn.name.equals("beanParam")).findFirst())
        .get(type(FieldNode.class))
        .extracting(fn -> fn.visibleAnnotations, list(AnnotationNode.class))
        .extracting(an -> an.desc);
  }

  private static Optional<MethodNode> fooMethod(ClassNode classNode) {
    return classNode.methods.stream().filter(mn -> mn.name.equals("foo")).findFirst();
  }

  static ClassNode loadIsolatedClass(Path dir, String className) throws IOException {
    return loadIsolatedClass(classFile(dir, className));
  }

  static ClassNode loadIsolatedClass(Path classFile) throws IOException {
    try (InputStream input = newInputStream(classFile)) {
      ClassReader classReader = new ClassReader(input);
      ClassNode classNode = new ClassNode(ASM9);
      classReader.accept(classNode, 0);
      return classNode;
    }
  }

  @SuppressWarnings("StringSplitter")
  static Path classFile(Path dir, String className) {
    Path f = dir;
    String[] parts = className.split("[.]");
    for (int i = 0; i < parts.length - 1; i++) {
      f = f.resolve(parts[i]);
    }
    return f.resolve(parts[parts.length - 1] + ".class");
  }
}
