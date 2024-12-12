/*
 * Copyright (C) 2021 The Authors of this project
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

val baseVersion = file("version.txt").readText().trim()

pluginManagement {
  plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
    id("com.diffplug.spotless") version "6.25.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
  }

  repositories {
    mavenCentral() // prefer Maven Central, in case Gradle's repo has issues
    gradlePluginPortal()
    if (System.getProperty("withMavenLocal").toBoolean()) {
      mavenLocal()
    }
  }
}

plugins { id("com.gradle.enterprise") version ("3.19") }

gradleEnterprise {
  if (System.getenv("CI") != null) {
    buildScan {
      termsOfServiceUrl = "https://gradle.com/terms-of-service"
      termsOfServiceAgree = "yes"
      // Add some potentially interesting information from the environment
      listOf(
          "GITHUB_ACTION_REPOSITORY",
          "GITHUB_ACTOR",
          "GITHUB_BASE_REF",
          "GITHUB_HEAD_REF",
          "GITHUB_JOB",
          "GITHUB_REF",
          "GITHUB_REPOSITORY",
          "GITHUB_RUN_ID",
          "GITHUB_RUN_NUMBER",
          "GITHUB_SHA",
          "GITHUB_WORKFLOW"
        )
        .forEach { e ->
          val v = System.getenv(e)
          if (v != null) {
            value(e, v)
          }
        }
      val ghUrl = System.getenv("GITHUB_SERVER_URL")
      if (ghUrl != null) {
        val ghRepo = System.getenv("GITHUB_REPOSITORY")
        val ghRunId = System.getenv("GITHUB_RUN_ID")
        link("Summary", "$ghUrl/$ghRepo/actions/runs/$ghRunId")
        link("PRs", "$ghUrl/$ghRepo/pulls")
      }
    }
  }
}

rootProject.name = "annotation-stripper"

gradle.beforeProject {
  group = "org.projectnessie.annotation-stripper"
  version = baseVersion
}

fun addProject(name: String) {
  include("annotation-stripper-$name")
  project(":annotation-stripper-$name").projectDir = file(name)
}

addProject("core")

addProject("plugin")
