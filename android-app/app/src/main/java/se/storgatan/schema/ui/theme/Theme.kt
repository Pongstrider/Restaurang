package se.storgatan.schema.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SchemaColorScheme = lightColorScheme(
    primary = Accent,
    onPrimary = OnAccent,
    primaryContainer = AccentLight,
    onPrimaryContainer = Accent,
    background = Background,
    onBackground = OnBackground,
    surface = CardBackground,
    onSurface = OnBackground,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextMuted,
    outline = CardBorder,
    outlineVariant = CardBorder,
    error = ErrorRed,
    onError = OnAccent,
)

private val SchemaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun SchemaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SchemaColorScheme,
        typography = SchemaTypography,
        shapes = SchemaShapes,
        content = content,
    )
}
