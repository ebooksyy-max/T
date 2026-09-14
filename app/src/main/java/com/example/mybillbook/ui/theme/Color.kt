package com.example.mybillbook.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * My Bill Book — Complete Light Color System
 * Strictly aligned to production branding standards.
 */
object BillBookColors {

    // Core Brand Colors
    val Primary = Color(0xFF1677FF)         // Bright Blue
    val PrimaryDark = Color(0xFF0B5ED7)     // Deep Blue
    val PrimaryLight = Color(0xFFE8F1FF)    // Light Blue
    val OnPrimary = Color(0xFFFFFFFF)

    val Secondary = Color(0xFF18B981)       // Mint Green
    val SecondaryDark = Color(0xFF0F9467)   // Green
    val SecondaryLight = Color(0xFFE6F8F1)  // Soft Mint
    val OnSecondary = Color(0xFFFFFFFF)

    val Tertiary = Color(0xFF7C4DFF)        // Purple
    val TertiaryDark = Color(0xFF6336D9)    // Deep Purple
    val TertiaryLight = Color(0xFFF0EAFF)   // Soft Purple
    val OnTertiary = Color(0xFFFFFFFF)

    // Background & Surface
    val Background = Color(0xFFF7F9FC)
    val OnBackground = Color(0xFF172033)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F4F8)
    val SurfaceContainer = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF9FAFC)
    val SurfaceContainerHigh = Color(0xFFEEF2F7)
    val OnSurface = Color(0xFF172033)
    val OnSurfaceVariant = Color(0xFF667085)
    val Outline = Color(0xFFD9E0EA)
    val OutlineVariant = Color(0xFFE8ECF2)

    // Text Colors & Typography Hierarchy
    val TextPrimary = Color(0xFF172033)
    val TextSecondary = Color(0xFF667085)
    val TextTertiary = Color(0xFF98A2B3)
    val TextDisabled = Color(0xFFB8C0CC)
    val TextOnPrimary = Color(0xFFFFFFFF)
    val TextOnSecondary = Color(0xFFFFFFFF)
    val LinkText = Color(0xFF1677FF)
    val Heading = Color(0xFF172033)
    val Body = Color(0xFF344054)
    val Caption = Color(0xFF98A2B3)
    val Disabled = Color(0xFFB8C0CC)

    // Financial Colors — Sales / Positive
    val SalesGreen = Color(0xFF18B981)
    val SalesGreenLight = Color(0xFFE6F8F1)
    val SalesGreenDark = Color(0xFF087A56)

    // Financial Colors — Receivable / Sales / Bills / Info
    val ReceivableBlue = Color(0xFF1677FF)
    val ReceivableBlueLight = Color(0xFFE8F1FF)
    val ReceivableBlueDark = Color(0xFF0B5ED7)

    // Financial Colors — Payable / Pending payment / Purchase
    val PayableOrange = Color(0xFFF79009)
    val PayableOrangeLight = Color(0xFFFFF4E5)
    val PayableOrangeDark = Color(0xFFC66A00)

    // Financial Colors — Due / Warning
    val DueAmber = Color(0xFFF5B700)
    val DueAmberLight = Color(0xFFFFF7D6)
    val DueAmberDark = Color(0xFFA66A00)

    // Financial Colors — Error / Cancelled
    val Error = Color(0xFFE5484D)
    val ErrorLight = Color(0xFFFDEBEC)
    val ErrorDark = Color(0xFFC62828)
    val OnError = Color(0xFFFFFFFF)

    // Information
    val Info = Color(0xFF3B82F6)
    val InfoLight = Color(0xFFEAF2FF)
    val InfoDark = Color(0xFF2563EB)

    // Buttons
    val ButtonPrimaryBg = Color(0xFF1677FF)
    val ButtonPrimaryText = Color(0xFFFFFFFF)
    val ButtonPrimaryPressed = Color(0xFF0B5ED7)
    val ButtonPrimaryDisabled = Color(0xFFD6E5FF)

    val ButtonSecondaryBg = Color(0xFFE8F1FF)
    val ButtonSecondaryText = Color(0xFF0B5ED7)
    val ButtonSecondaryBorder = Color(0xFFB9D2FF)

    val ButtonSuccessBg = Color(0xFF18B981)
    val ButtonSuccessText = Color(0xFFFFFFFF)
    val ButtonSuccessPressed = Color(0xFF0F9467)

    val ButtonDangerBg = Color(0xFFE5484D)
    val ButtonDangerText = Color(0xFFFFFFFF)

    // Status Chips
    val ChipPaidContainer = Color(0xFFE6F8F1)
    val ChipPaidText = Color(0xFF087A56)

    val ChipPartiallyPaidContainer = Color(0xFFFFF4E5)
    val ChipPartiallyPaidText = Color(0xFFC66A00)

    val ChipUnpaidContainer = Color(0xFFFDEBEC)
    val ChipUnpaidText = Color(0xFFC62828)

    val ChipDraftContainer = Color(0xFFF1F4F8)
    val ChipDraftText = Color(0xFF667085)

    val ChipCancelledContainer = Color(0xFFFDEBEC)
    val ChipCancelledText = Color(0xFFC62828)

    val ChipDueContainer = Color(0xFFFFF7D6)
    val ChipDueText = Color(0xFFA66A00)

    // Customer UI
    val CustomerIcon = Color(0xFF1677FF)
    val CustomerIconContainer = Color(0xFFE8F1FF)
    val CustomerBalanceReceivable = Color(0xFF1677FF)
    val CustomerPayment = Color(0xFF18B981)
    val CustomerDue = Color(0xFFF79009)

    // Product & Inventory UI
    val StockNormalIcon = Color(0xFF1677FF)
    val StockNormalContainer = Color(0xFFE8F1FF)
    val StockLowIcon = Color(0xFFF79009)
    val StockLowContainer = Color(0xFFFFF4E5)
    val StockOutIcon = Color(0xFFE5484D)
    val StockOutContainer = Color(0xFFFDEBEC)
    val StockIncreased = Color(0xFF18B981)
    val StockDecreased = Color(0xFFF79009)

    // Invoice UI
    val InvoiceHeader = Color(0xFF1677FF)
    val InvoiceTotal = Color(0xFF172033)
    val InvoicePaid = Color(0xFF18B981)
    val InvoiceDue = Color(0xFFF79009)
    val InvoiceDivider = Color(0xFFE8ECF2)
    val InvoiceBackground = Color(0xFFFFFFFF)

    // Icons
    val IconPrimary = Color(0xFF1677FF)
    val IconSecondary = Color(0xFF667085)
    val IconSuccess = Color(0xFF18B981)
    val IconWarning = Color(0xFFF79009)
    val IconError = Color(0xFFE5484D)
    val IconPurpleFeature = Color(0xFF7C4DFF)

    // Report & Chart Colors
    val ChartBlue = Color(0xFF1677FF)
    val ChartGreen = Color(0xFF18B981)
    val ChartPurple = Color(0xFF7C4DFF)
    val ChartOrange = Color(0xFFF79009)
    val ChartRed = Color(0xFFE5484D)
    val ChartCyan = Color(0xFF16B8D4)
    val ChartYellow = Color(0xFFF5B700)
    val ChartTeal = Color(0xFF0FA3B1)

    // Chart Payment Modes
    val ChartCash = Color(0xFF1677FF)
    val ChartUpi = Color(0xFF18B981)
    val ChartCard = Color(0xFF7C4DFF)
    val ChartBank = Color(0xFFF79009)
    val ChartOther = Color(0xFF16B8D4)
}

// Top-level semantic aliases for backwards compatibility with existing UI calls
val PrimaryBlue = BillBookColors.Primary
val PrimaryBlueVariant = BillBookColors.PrimaryDark
val PrimaryBlueLight = BillBookColors.PrimaryLight
val SecondaryTeal = BillBookColors.Secondary
val SecondaryTealLight = BillBookColors.SecondaryLight

val BackgroundLight = BillBookColors.Background
val SurfaceLight = BillBookColors.Surface
val SurfaceVariantLight = BillBookColors.SurfaceVariant
val OutlineLight = BillBookColors.Outline
val OutlineVariantLight = BillBookColors.OutlineVariant

val TextPrimary = BillBookColors.TextPrimary
val TextSecondary = BillBookColors.TextSecondary
val TextMuted = BillBookColors.TextTertiary

val SuccessGreen = BillBookColors.SalesGreen
val SuccessGreenLight = BillBookColors.SalesGreenLight
val WarningAmber = BillBookColors.PayableOrange
val WarningAmberLight = BillBookColors.PayableOrangeLight
val DangerRed = BillBookColors.Error
val DangerRedLight = BillBookColors.ErrorLight
val InfoSky = BillBookColors.Info
val InfoSkyLight = BillBookColors.InfoLight

// Dark Theme Fallbacks
val PrimaryBlueDark = Color(0xFF82B1FF)
val BackgroundDark = Color(0xFF0D131F)
val SurfaceDark = Color(0xFF172033)
val SurfaceVariantDark = Color(0xFF222F46)
val OutlineDark = Color(0xFF384760)
val TextPrimaryDark = Color(0xFFF7F9FC)
val TextSecondaryDark = Color(0xFF98A2B3)
