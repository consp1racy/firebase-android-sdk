// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.gradle.plugins.ci

import com.google.gson.Gson
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class ChangedModulesTask : DefaultTask() {
  @get:Input
  @set:Option(option = "changed-git-paths", description = "Hellos")
  abstract var changedGitPaths: List<String>

  @get:Input
  @set:Option(option = "output-file-path", description = "Hello")
  abstract var outputFilePath: String

  @get:OutputFile val outputFile by lazy { File(outputFilePath) }

  init {
    outputs.upToDateWhen { false }
  }

  @TaskAction
  fun execute() {
    val projects =
      AffectedProjectFinder(project, changedGitPaths.toSet(), listOf())
        .find()
        .map { it.path }
        .toSet()

    val result = project.rootProject.subprojects.associate { it.path to mutableSetOf<String>() }
    project.rootProject.subprojects.forEach { p ->
      p.configurations.forEach { c ->
        c.dependencies.filterIsInstance<ProjectDependency>().forEach {
          result[it.dependencyProject.path]?.add(p.path)
        }
      }
    }
    val affectedProjects =
      result
        .flatMap { (key, value) -> if (projects.contains(key)) setOf(key) + value else setOf() }
        .toSet()

    outputFile.writeText(Gson().toJson(affectedProjects))
  }
}
