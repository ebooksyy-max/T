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
import androidx.compose.ui.unit.sp
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = PrimaryBlue,
    iconBg: Color = PrimaryBlueLight,
    valueColor: Color = TextPrimary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag("summary_card_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = CurrencyUtils.format(amount),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        "PAID" -> Pair(SuccessGreenLight, SuccessGreen)
        "PARTIALLY_PAID" -> Pair(WarningAmberLight, WarningAmber)
        "UNPAID" -> Pair(DangerRedLight, DangerRed)
        "CANCELLED" -> Pair(OutlineLight, TextMuted)
        "DRAFT" -> Pair(InfoSkyLight, InfoSky)
        "LOW_STOCK" -> Pair(DangerRedLight, DangerRed)
        "IN_STOCK" -> Pair(SuccessGreenLight, SuccessGreen)
        else -> Pair(SurfaceVariantLight, TextSecondary)
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
        true -> SuccessGreen
        false -> DangerRed
        null -> MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = CurrencyUtils.format(amount),
        style = style.copy(fontWeight = FontWeight.SemiBold),
        color = color,
        modifier = modifier
    )
}
