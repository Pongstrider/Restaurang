package se.storgatan.schema.ui.screens.release

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.AccentLight
import se.storgatan.schema.ui.theme.CardBorder
import se.storgatan.schema.ui.theme.TextMuted

@Composable
fun ReleaseShiftScreen(
    onBack: () -> Unit,
    onReleased: () -> Unit,
    viewModel: ReleaseShiftViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) onReleased()
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
            Text("Lägg ut pass", style = MaterialTheme.typography.titleLarge)
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            state.shift != null -> {
                val shift = state.shift!!
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(ShiftFormat.longDate(shift.date), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "${ShiftFormat.type(shift.type)} ${ShiftFormat.time(shift.start, shift.end)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.reason,
                        onValueChange = viewModel::onReasonChange,
                        label = { Text("Anledning (valfritt)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        enabled = !state.isSubmitting,
                    )

                    Spacer(Modifier.height(24.dp))
                    Text("Vem ska kunna ta passet?", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Card(
                        onClick = viewModel::selectAll,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, if (state.targetAll) Accent else CardBorder),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.targetAll) AccentLight else MaterialTheme.colorScheme.surface,
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = state.targetAll,
                                onClick = viewModel::selectAll,
                                colors = RadioButtonDefaults.colors(selectedColor = Accent),
                            )
                            Text(
                                "Alla kollegor",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    state.colleagues.forEach { colleague ->
                        val selected = !state.targetAll && state.selectedColleague?.id == colleague.id
                        Card(
                            onClick = { viewModel.selectColleague(colleague) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            border = BorderStroke(1.dp, if (selected) Accent else CardBorder),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) AccentLight else MaterialTheme.colorScheme.surface,
                            ),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.selectColleague(colleague) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Accent),
                                )
                                Text(colleague.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AccentLight),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            "Passet är ditt tills någon tar det. Du kan ångra utläggningen så länge.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }

                    if (state.error != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::release,
                        enabled = !state.isSubmitting && (state.targetAll || state.selectedColleague != null),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Lägg ut passet")
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
