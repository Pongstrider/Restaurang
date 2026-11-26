package se.storgatan.schema.ui.screens.available

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.storgatan.schema.domain.ShiftFormat
import se.storgatan.schema.ui.components.TypeBadge
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.CardBorder
import se.storgatan.schema.ui.theme.TextMuted
import se.storgatan.schema.ui.theme.WarningBg
import se.storgatan.schema.ui.theme.WarningText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableShiftsScreen(
    onShiftClaimed: (String) -> Unit,
    viewModel: AvailableShiftsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.claimedShiftId) {
        state.claimedShiftId?.let {
            onShiftClaimed(it)
            viewModel.clearClaimed()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Lediga pass",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Inga lediga pass", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Just nu finns inga pass att ta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (state.error != null) {
                            item {
                                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        items(state.items, key = { it.shift.id }) { item ->
                            AvailableShiftCard(
                                item = item,
                                isClaiming = state.claimingId == item.shift.id,
                                onClaim = { viewModel.claim(item.shift.id) },
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableShiftCard(
    item: AvailableShiftItem,
    isClaiming: Boolean,
    onClaim: () -> Unit,
) {
    val shift = item.shift
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.conflictsWithMine) 0.5f else 1f),
        border = BorderStroke(1.dp, CardBorder),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(ShiftFormat.longDate(shift.date), style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${ShiftFormat.type(shift.type)} ${ShiftFormat.time(shift.start, shift.end)}  ·  ${ShiftFormat.role(shift.role)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )
                }
                TypeBadge(shift)
            }

            Spacer(Modifier.height(8.dp))

            shift.releasedBy?.let { by ->
                Text(
                    "Utlagt av ${by.name}" + (shift.releasedAt?.let { " · ${ShiftFormat.relative(it, java.time.Instant.now())}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            shift.releaseReason?.let { reason ->
                Text("\"$reason\"", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }

            Spacer(Modifier.height(12.dp))

            if (item.conflictsWithMine) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningBg),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        "Krockar med ditt pass" + (item.conflictingShift?.let { " (${ShiftFormat.describe(it)})" } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        color = WarningText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            } else {
                Button(
                    onClick = onClaim,
                    enabled = !isClaiming,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    if (isClaiming) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text("Ta passet")
                    }
                }
            }
        }
    }
}
