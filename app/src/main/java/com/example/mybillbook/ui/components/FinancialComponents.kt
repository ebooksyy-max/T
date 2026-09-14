package com.example.mybillbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = BillBookColors.Primary,
    iconBg: Color = BillBookColors.PrimaryLight,
    valueColor: Color = BillBookColors.TextPrimary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag("summary_card_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BillBookColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = BillBookColors.TextSecondary,
                    maxLines = 2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = CurrencyUtils.format(amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
                maxLines = 1
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = BillBookColors.TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bg, text) = when (status.uppercase()) {
        "PAID" -> Pair(BillBookColors.ChipPaidContainer, BillBookColors.ChipPaidText)
        "PARTIALLY_PAID" -> Pair(BillBookColors.ChipPartiallyPaidContainer, BillBookColors.ChipPartiallyPaidText)
        "UNPAID" -> Pair(BillBookColors.ChipUnpaidContainer, BillBookColors.ChipUnpaidText)
        "CANCELLED" -> Pair(BillBookColors.ChipCancelledContainer, BillBookColors.ChipCancelledText)
        "DRAFT" -> Pair(BillBookColors.ChipDraftContainer, BillBookColors.ChipDraftText)
        "DUE" -> Pair(BillBookColors.ChipDueContainer, BillBookColors.ChipDueText)
        "LOW_STOCK" -> Pair(BillBookColors.StockLowContainer, BillBookColors.StockLowIcon)
        "OUT_OF_STOCK" -> Pair(BillBookColors.StockOutContainer, BillBookColors.StockOutIcon)
        "IN_STOCK" -> Pair(BillBookColors.ChipPaidContainer, BillBookColors.ChipPaidText)
        else -> Pair(BillBookColors.SurfaceVariant, BillBookColors.TextSecondary)
    }

    Surface(
        modifier = modifier.testTag("status_chip_${status.lowercase()}"),
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = status.replace("_", " "),
            color = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    isIncomeOrReceivable: Boolean? = null,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    val color = when (isIncomeOrReceivable) {
        true -> BillBookColors.SalesGreen
        false -> BillBookColors.Error
        null -> BillBookColors.TextPrimary
    }

    Text(
        text = CurrencyUtils.format(amount),
        style = style.copy(fontWeight = FontWeight.SemiBold),
        color = color,
        modifier = modifier
    )
}
