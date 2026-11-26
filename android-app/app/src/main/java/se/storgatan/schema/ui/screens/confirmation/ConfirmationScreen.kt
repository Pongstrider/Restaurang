package se.storgatan.schema.ui.screens.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.storgatan.schema.domain.ShiftFormat
import se.storgatan.schema.ui.theme.Accent
import se.storgatan.schema.ui.theme.AccentLight

@Composable
fun ConfirmationScreen(
    onDone: () -> Unit,
    viewModel: ConfirmationViewModel = hiltViewModel(),
) {
    val shift by viewModel.shift.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Passet är ditt!", style = MaterialTheme.typography.headlineMedium)

        shift?.let { s ->
            Spacer(Modifier.height(8.dp))
            Text(
                "${ShiftFormat.longDate(s.date)}, ${ShiftFormat.type(s.type).lowercase()} ${ShiftFormat.time(s.start, s.end)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AccentLight),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ConfirmRow("Schemat är uppdaterat")
                ConfirmRow("Din kollega har fått besked")
                ConfirmRow("Anders har fått en notis")
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            Text("Tillbaka till lediga pass")
        }
    }
}

@Composable
private fun ConfirmRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.padding(start = 8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
