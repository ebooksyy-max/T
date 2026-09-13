package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("My Bill Book", appName)
  }

  @Test
  fun `invoice number generator formats correctly`() {
    val invoiceNo = com.example.mybillbook.utils.InvoiceNumberGenerator.generateInvoiceNumber("INV-", 42L)
    assertEquals("INV-0042", invoiceNo)
  }

  @Test
  fun `currency utils formats correctly`() {
    val formatted = com.example.mybillbook.utils.CurrencyUtils.format(1250.50)
    assertTrue(formatted.contains("1,250.50"))
  }
}
