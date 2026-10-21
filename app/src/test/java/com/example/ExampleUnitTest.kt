package com.example

import com.example.util.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testTimeFormatting_subMinute() {
    val millis = 45_250L // 45 seconds, 25 hundredths
    val formatted = TimeFormatter.format(millis)
    assertEquals("00:45.25", formatted)
  }

  @Test
  fun testTimeFormatting_withHours() {
    val millis = 3_661_500L // 1 hour, 1 min, 1 sec, 50 hundredths
    val formatted = TimeFormatter.format(millis)
    assertEquals("01:01:01.50", formatted)
  }

  @Test
  fun testLapDiffFormatting() {
    val positiveDiff = 1250L
    assertEquals("+00:01.25", TimeFormatter.formatLapDiff(positiveDiff))

    val negativeDiff = -850L
    assertEquals("-00:00.85", TimeFormatter.formatLapDiff(negativeDiff))
  }
}
