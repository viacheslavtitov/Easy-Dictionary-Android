package org.easydictionary.app

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic

fun setUpMockLog() {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0
    every { Log.i(any(), any()) } returns 0
    every { Log.w(any(), any<String>()) } returns 0
    every { Log.w(any(), any<Throwable>()) } returns 0
    every { Log.e(any(), any()) } returns 0
}