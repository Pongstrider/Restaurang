package se.storgatan.schema.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.storgatan.schema.domain.ShiftFormat
import se.storgatan.schema.ui.components.ReleasedBadge
import se.storgatan.schema.ui.components.TypeBadge
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.AccentLight
import se.storgatan.schema.ui.theme.CardBorder
import se.storgatan.schema.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftDetailScreen(
    onBack: () -> Unit,
    onRelease: (String) -> Unit,
    onUnreleased: () -> Unit,
    viewModel: ShiftDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.unreleaseSuccess) {
        if (state.unreleaseSuccess) onUnreleased()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
            }
            Text("Passdetalj", style = MaterialTheme.typography.titleLarge)
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            state.error != null && state.shift == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            state.shift != null -> {
                val shift = state.shift!!
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ShiftFormat.longDate(shift.date),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TypeBadge(shift)
                        if (shift.isReleased) ReleasedBadge()
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${ShiftFormat.time(shift.start, shift.end)}  ·  ${ShiftFormat.role(shift.role)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                    )

                    if (shift.coworkers.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text("Jobbar också", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, CardBorder),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            FlowRow(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                shift.coworkers.forEach { cw ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = AccentLight),
                                        shape = MaterialTheme.shapes.small,
                                    ) {
                                        Text(
                                            "${cw.name} · ${ShiftFormat.role(cw.role)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.isMine) {
                        Spacer(Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AccentLight),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                "Vill du lägga ut passet kan en kollega ta det direkt – Anders behöver inte godkänna.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        if (shift.isReleased) {
                            OutlinedButton(
                                onClick = viewModel::unrelease,
                                enabled = !state.isUnreleasing,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                if (state.isUnreleasing) {
                                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Ångra utläggningen")
                                }
                            }
                        } else {
                            Button(
                                onClick = { onRelease(shift.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            ) {
                                Text("Lägg ut passet")
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onRelease(shift.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text("Byt direkt med en kollega")
                            }
                        }
                    }

                    if (state.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
