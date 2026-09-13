package com.example.mybillbook.domain.usecase

import com.example.mybillbook.data.entity.InvoiceStatus
import com.example.mybillbook.domain.model.BillCalculation
import com.example.mybillbook.domain.model.BillItemInput
import com.example.mybillbook.domain.model.CalculatedItem
import kotlin.math.max
import kotlin.math.round

object BillingCalculator {

    fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }

    fun calculateItem(item: BillItemInput): CalculatedItem {
        val qty = max(0.0, item.quantity)
        val grossAmount = roundToTwoDecimals(qty * item.rate)
        val discount = minOf(grossAmount, max(0.0, item.discount))
        val afterDiscount = max(0.0, grossAmount - discount)

        val baseAmount: Double
        val taxAmount: Double
        val totalAmount: Double

        if (item.taxRate <= 0.0) {
            baseAmount = afterDiscount
            taxAmount = 0.0
            totalAmount = afterDiscount
        } else if (item.isTaxInclusive) {
            // Price includes tax: baseAmount = afterDiscount / (1 + rate/100)
            baseAmount = roundToTwoDecimals(afterDiscount / (1.0 + (item.taxRate / 100.0)))
            taxAmount = roundToTwoDecimals(afterDiscount - baseAmount)
            totalAmount = afterDiscount
        } else {
            // Price excludes tax: tax = afterDiscount * (rate/100)
            baseAmount = afterDiscount
            taxAmount = roundToTwoDecimals(baseAmount * (item.taxRate / 100.0))
            totalAmount = roundToTwoDecimals(baseAmount + taxAmount)
        }

        return CalculatedItem(
            productId = item.productId,
            productName = item.productName,
            quantity = qty,
            unit = item.unit,
            rate = item.rate,
            discount = discount,
            taxRate = item.taxRate,
            baseAmount = baseAmount,
            taxAmount = taxAmount,
            totalAmount = totalAmount
        )
    }

    fun calculateBill(
        items: List<BillItemInput>,
        additionalDiscount: Double = 0.0,
        receivedAmount: Double = 0.0,
        businessState: String = "",
        customerState: String = "",
        isDraft: Boolean = false
    ): BillCalculation {
        val calculatedItems = items.map { calculateItem(it) }

        val subtotal = roundToTwoDecimals(calculatedItems.sumOf { it.baseAmount })
        val itemDiscounts = roundToTwoDecimals(calculatedItems.sumOf { it.discount })
        val totalItemTax = roundToTwoDecimals(calculatedItems.sumOf { it.taxAmount })

        val billDiscount = roundToTwoDecimals(max(0.0, minOf(subtotal, additionalDiscount)))
        val totalDiscount = roundToTwoDecimals(itemDiscounts + billDiscount)

        val taxableAmount = roundToTwoDecimals(max(0.0, subtotal - billDiscount))
        // If billDiscount is applied at whole bill level, adjust tax proportionally if tax is present
        val taxAmount = if (subtotal > 0 && billDiscount > 0) {
            roundToTwoDecimals(totalItemTax * (taxableAmount / subtotal))
        } else {
            totalItemTax
        }

        val grandTotal = roundToTwoDecimals(taxableAmount + taxAmount)
        val validReceived = roundToTwoDecimals(max(0.0, receivedAmount))
        val dueAmount = roundToTwoDecimals(max(0.0, grandTotal - validReceived))

        // Inter-state check (IGST vs CGST+SGST)
        val isInterState = businessState.isNotBlank() &&
                customerState.isNotBlank() &&
                !businessState.equals(customerState, ignoreCase = true)

        val igstAmount = if (isInterState) taxAmount else 0.0
        val cgstAmount = if (!isInterState) roundToTwoDecimals(taxAmount / 2.0) else 0.0
        val sgstAmount = if (!isInterState) roundToTwoDecimals(taxAmount - cgstAmount) else 0.0

        val status = when {
            isDraft -> InvoiceStatus.DRAFT
            dueAmount == 0.0 -> InvoiceStatus.PAID
            validReceived > 0.0 && dueAmount > 0.0 -> InvoiceStatus.PARTIALLY_PAID
            else -> InvoiceStatus.UNPAID
        }

        return BillCalculation(
            items = calculatedItems,
            subtotal = subtotal,
            billDiscount = billDiscount,
            totalDiscount = totalDiscount,
            taxableAmount = taxableAmount,
            taxAmount = taxAmount,
            cgstAmount = cgstAmount,
            sgstAmount = sgstAmount,
            igstAmount = igstAmount,
            isInterState = isInterState,
            grandTotal = grandTotal,
            receivedAmount = validReceived,
            dueAmount = dueAmount,
            status = status
        )
    }
}
