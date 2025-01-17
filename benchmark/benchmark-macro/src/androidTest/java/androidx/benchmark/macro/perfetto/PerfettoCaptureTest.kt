/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.benchmark.macro.perfetto

import androidx.benchmark.macro.FileLinkingRule
import androidx.benchmark.macro.Packages
import androidx.benchmark.perfetto.PerfettoCapture
import androidx.benchmark.perfetto.PerfettoHelper
import androidx.benchmark.perfetto.PerfettoHelper.Companion.LOWEST_BUNDLED_VERSION_SUPPORTED
import androidx.benchmark.perfetto.PerfettoHelper.Companion.isAbiSupported
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import androidx.testutils.verifyWithPolling
import androidx.tracing.Trace
import androidx.tracing.trace
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for PerfettoCapture
 *
 * Note: this test is defined in benchmark-macro instead of benchmark-common so that it can
 * validate trace contents with PerfettoTraceProcessor
 */
@SdkSuppress(minSdkVersion = 23)
@RunWith(AndroidJUnit4::class)
class PerfettoCaptureTest {
    @get:Rule
    val linkRule = FileLinkingRule()

    @Before
    @After
    fun cleanup() {
        PerfettoHelper.stopAllPerfettoProcesses()
    }

    @SdkSuppress(
        minSdkVersion = 21,
        maxSdkVersion = LOWEST_BUNDLED_VERSION_SUPPORTED - 1
    )
    @SmallTest
    @Test
    fun bundledNotSupported() {
        assumeTrue(isAbiSupported())

        assertFailsWith<IllegalArgumentException> {
            PerfettoCapture(false)
        }
    }

    @SdkSuppress(minSdkVersion = LOWEST_BUNDLED_VERSION_SUPPORTED)
    @LargeTest
    @Test
    fun captureAndValidateTrace_bundled() = captureAndValidateTrace(unbundled = false)

    @LargeTest
    @Test
    fun captureAndValidateTrace_unbundled() = captureAndValidateTrace(unbundled = true)

    private fun captureAndValidateTrace(unbundled: Boolean) {
        assumeTrue(isAbiSupported())

        val traceFilePath = linkRule.createReportedTracePath(Packages.TEST)
        val perfettoCapture = PerfettoCapture(unbundled)

        verifyTraceEnable(false)

        perfettoCapture.start(listOf(Packages.TEST))

        verifyTraceEnable(true)

        // TODO: figure out why this sleep (200ms+) is needed - possibly related to b/194105203
        Thread.sleep(500)

        // Tracing non-trivial duration for manual debugging/verification
        trace(CUSTOM_TRACE_SECTION_LABEL_1) { Thread.sleep(20) }
        trace(CUSTOM_TRACE_SECTION_LABEL_2) { Thread.sleep(20) }

        perfettoCapture.stop(traceFilePath)

        val matchingSlices = PerfettoTraceProcessor.querySlices(
            absoluteTracePath = traceFilePath,
            CUSTOM_TRACE_SECTION_LABEL_1,
            CUSTOM_TRACE_SECTION_LABEL_2
        )

        // Note: this test avoids validating platform-triggered trace sections, to avoid flakes
        // from legitimate (and coincidental) platform use during test.
        assertEquals(
            listOf(CUSTOM_TRACE_SECTION_LABEL_1, CUSTOM_TRACE_SECTION_LABEL_2),
            matchingSlices.sortedBy { it.ts }.map { it.name }
        )
        matchingSlices
            .forEach {
                assertTrue(it.dur > 15_000_000) // should be at least 15ms
            }
    }

    companion object {
        const val CUSTOM_TRACE_SECTION_LABEL_1 = "PerfettoCaptureTest_1"
        const val CUSTOM_TRACE_SECTION_LABEL_2 = "PerfettoCaptureTest_2"
    }
}

fun verifyTraceEnable(enabled: Boolean) {
    // We poll here, since we may need to wait for enable flags to propagate to apps
    verifyWithPolling(
        "Timeout waiting for Trace.isEnabled == $enabled",
        periodMs = 50,
        timeoutMs = 5000
    ) {
        Trace.isEnabled() == enabled
    }
}
