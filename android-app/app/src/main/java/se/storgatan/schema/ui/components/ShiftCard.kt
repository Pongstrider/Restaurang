package se.storgatan.schema.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.storgatan.schema.domain.Shift
import se.storgatan.schema.domain.ShiftFormat
import se.storgatan.schema.domain.ShiftType
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.AccentLight
import se.storgatan.schema.ui.theme.CardBorder
import se.storgatan.schema.ui.theme.LunchBg
import se.storgatan.schema.ui.theme.LunchText
import se.storgatan.schema.ui.theme.WarningBg
import se.storgatan.schema.ui.theme.WarningText

@Composable
fun TypeBadge(shift: Shift) {
    val (bg, text) = if (shift.type == ShiftType.LUNCH) LunchBg to LunchText else AccentLight to Accent
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = ShiftFormat.type(shift.type),
            style = MaterialTheme.typography.labelMedium,
            color = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun ReleasedBadge() {
    Card(
        colors = CardDefaults.cardColors(containerColor = WarningBg),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = "Utlagt",
            style = MaterialTheme.typography.labelMedium,
            color = WarningText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NextShiftCard(shift: Shift, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Accent),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Nästa pass", style = MaterialTheme.typography.labelMedium, color = AccentLight)
            Spacer(Modifier.height(8.dp))
            Text(
                ShiftFormat.longDate(shift.date),
                style = MaterialTheme.typography.headlineSmall,
                color = AccentLight,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${ShiftFormat.type(shift.type)} ${ShiftFormat.time(shift.start, shift.end)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentLight,
                )
            }
            if (shift.coworkers.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Jobbar också:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentLight.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    shift.coworkers.forEach { cw ->
                        Text(cw.name, style = MaterialTheme.typography.bodyMedium, color = AccentLight)
                    }
                }
            }
            if (shift.isReleased) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningBg),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        "Utlagt",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftListCard(shift: Shift, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    ShiftFormat.longDate(shift.date),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${ShiftFormat.type(shift.type)} ${ShiftFormat.time(shift.start, shift.end)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (shift.coworkers.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        shift.coworkers.forEach { cw ->
                            Text(cw.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                TypeBadge(shift)
                if (shift.isReleased) {
                    Spacer(Modifier.height(4.dp))
                    ReleasedBadge()
                }
            }
        }
    }
}
