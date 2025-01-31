/*
 * Copyright (c) 2021 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

abstract class AbstractBufIntegrationTest {
    @TempDir
    lateinit var projectDir: File

    private lateinit var settingsFile: File
    lateinit var buildFile: File
    lateinit var protoDir: File

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        settingsFile = File(projectDir, "settings.gradle")
        buildFile = File(projectDir, "build.gradle")
        protoDir = Path.of(projectDir.path, "src", "main", "proto").toFile()

        settingsFile.writeText("rootProject.name = 'testing'")

        val fixture = File("src/test/resources/${testInfo.testClass.get().simpleName}/${testInfo.testMethod.get().name}")
        if (fixture.exists()) {
            assertWithMessage("Failed to copy test fixture files").that(fixture.copyRecursively(projectDir)).isTrue()
        }
    }

    class WrappedRunner(
        private val delegate: GradleRunner
    ) {
        fun withArguments(vararg args: String) =
            WrappedRunner(delegate.withArguments(delegate.arguments + args))

        fun build() =
            delegate.build().also { println(it.output) }

        fun buildAndFail() =
            delegate.buildAndFail().also { println(it.output) }
    }

    fun gradleRunner() =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("-PprotobufGradleVersion=0.8.18", "-PprotobufVersion=3.19.4")
            .let { WrappedRunner(it) }

    fun checkRunner() =
        gradleRunner().withArguments("check")

    fun publishRunner() =
        gradleRunner().withArguments("publish")
}
