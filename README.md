# Annotation Stripper

[![Group Discussion](https://img.shields.io/badge/Discussion-Groups-blue.svg?color=3d4db3&logo=google&style=for-the-badge&logoColor=white)](https://groups.google.com/g/projectnessie)
[![Twitter](https://img.shields.io/badge/Twitter-Follow_Us-blue?color=3d4db3&logo=twitter&style=for-the-badge&logoColor=white)](https://twitter.com/projectnessie)
[![Website](https://img.shields.io/badge/https-projectnessie.org-blue?color=3d4db3&logo=firefox&style=for-the-badge&logoColor=white)](https://projectnessie.org/)

[![Build Status](https://img.shields.io/github/actions/workflow/status/projectnessie/annotation-stripper/main.yml?label=Main%20CI&logo=Github&style=for-the-badge)](https://github.com/projectnessie/annotation-stripper/actions/workflows/main.yml?query=branch%3Amain)
[![Maven Central](https://img.shields.io/maven-central/v/org.projectnessie.annotation-stripper/annotation-stripper?label=Maven%20Central&logo=apachemaven&color=3f6ec6&style=for-the-badge&logoColor=white)](https://search.maven.org/artifact/org.projectnessie.annotation-stripper/annotation-stripper)

Core classes and Gradle plugin to strip requested annotations from compiled classes.

Originally build to hide Jakarta EE annotations from Java 8 (see [Motivation / Background](#motivation--background)).

## Gradle plugin / Kotlin DSL

```kotlin
plugins {
  `java-library`
  id("org.projectnessie.annotation-stripper") version "0.1.2"
}

dependencies {
  compileOnly("javax.ws.rs:javax.ws.rs-api:2.1.1")
  compileOnly("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
}

annotationStripper {
  // registerDefault() registers annotation stripping on the "main" source set.
  registerDefault().configure {
    // Drop all annotations in the jakarta.* "namespace"
    annotationsToDrop("^jakarta[.].+".toRegex())
    // but keep the annotation for Java 11 and newer.
    unmodifiedClassesForJavaVersion.set(11)
    // The resulting class files (that end in the jar for this sample project) contains classes
    // without the jakarta.* annotations for Java 8, but keeps both javax.* and jakarta.*
    // annotations for Java 11 and newer by effectively producing a multi-release jar.
  }
}
```

## Gradle plugin / Groovy DSL

```groovy
plugins {
  id 'java-library'
  id 'org.projectnessie.annotation-stripper' version '0.1.2'
}

dependencies {
  compileOnly 'javax.ws.rs:javax.ws.rs-api:2.1.1'
  compileOnly 'jakarta.ws.rs:jakarta.ws.rs-api:3.1.0'
}

annotationStripper {
  // registerDefault() registers annotation stripping on the "main" source set.
  registerDefault().configure {
    // Drop all annotations in the jakarta.* "namespace"
    annotationsToDrop(~'^jakarta[.].+')
    // but keep the annotation for Java 11 and newer.
    unmodifiedClassesForJavaVersion.set(11)
    // The resulting class files (that end in the jar for this sample project) contains classes
    // without the jakarta.* annotations for Java 8, but keeps both javax.* and jakarta.*
    // annotations for Java 11 and newer by effectively producing a multi-release jar.
  }
}
```

## Maven plugin

Not there - contributions are welcome!

## Motivation / Background

This plugin originates from an effort to work around the old bug
[JDK-8152174](https://bugs.openjdk.org/browse/JDK-8152174), which was fixed in July 2016 for
Java 9, but was never back-ported to Java 8.

Today, with Jakarta EE APIs being built for Java 11+ and the `javax.`->`jakarta.` rename, and
our mandate to support [Nessie](https://githubcom/projectnessie/nessie) clients with Java 8 and
Java 11 or newer, while the Nessie Quarkus servers run on Java 11 or newer, the most viable
solution was to "duplicate" the `javax.*` annotations with their `jakarta.*` counterparts.

For example a REST class like this
```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/hello")
class FooResource {
  @GET
  String helloWorld(@PathParam String echoMe) {
    return echoMe;
  }
} 
```
became
```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/hello")
@jakarta.ws.rs.Path("/path")
class FooResource {
  @GET
  @jakarta.ws.rs.GET
  String helloWorld(@PathParam @jakarta.ws.rs.PathParam("meep") String echoMe) {
    return echoMe;
  }
} 
```

The approach look~~s~~^Hed very promising, it relies on the Java specification, which says that
annotations that are not present must be ignored (aka cannot be inspected via reflection).
So old clients (but also servers) that use Java EE (aka `javax.*` annotations) work, but also
Jakarta EE clients and servers work, for example Quarkus 3 or newer.

And it does! Until the code is run on even the newest Java 8, because of
[JDK-8152174](https://bugs.openjdk.org/browse/JDK-8152174).

