package se.storgatan.schema.ui.screens.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ShiftDetailScreen(
    onBack: () -> Unit,
    onRelease: (String) -> Unit,
    onUnreleased: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Passdetalj – byts ut i steg 7")
    }
}
