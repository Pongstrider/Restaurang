package com.example.demo1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RestaurantApp()
            }
        }
    }
}

enum class Screen {
    BordsOversikt,
    Bestallning
}

@Composable
fun RestaurantApp() {
    var currentScreen by remember { mutableStateOf(Screen.BordsOversikt) }
    var selectedCategory by remember { mutableStateOf("Dryck") }
    var bookingFilter by remember { mutableStateOf("Alla") }

    val bgBackgroundColor = Color(0xFFF4F3ED)
    val darkGreen = Color(0xFF2F4F3E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBackgroundColor)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Navigation between Menus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANTONS SKAFFERI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = if (currentScreen == Screen.BordsOversikt) "Bordsöversikt" else "Beställ från bordet",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D2421)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu Switcher Buttons (as requested: "klicka mellan menyerna")
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (currentScreen == Screen.BordsOversikt) darkGreen else Color.White,
                        border = BorderStroke(1.dp, darkGreen),
                        modifier = Modifier.clickable { currentScreen = Screen.BordsOversikt }
                    ) {
                        Text(
                            text = "Bordsöversikt",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = if (currentScreen == Screen.BordsOversikt) Color.White else darkGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = if (currentScreen == Screen.Bestallning) darkGreen else Color.White,
                        border = BorderStroke(1.dp, darkGreen),
                        modifier = Modifier.clickable { currentScreen = Screen.Bestallning }
                    ) {
                        Text(
                            text = "Beställ från bordet (Bord 05)",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            color = if (currentScreen == Screen.Bestallning) Color.White else darkGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (currentScreen == Screen.BordsOversikt) {
                        Text(
                            text = "PERSONALVY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Button(
                            onClick = { /* No-op as requested */ },
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = "Ny bokning", color = Color.White)
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8E9E7),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "BORD 05",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D2421),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content based on selected screen
            if (currentScreen == Screen.BordsOversikt) {
                BordsOversiktScreen(
                    bookingFilter = bookingFilter,
                    onFilterChange = { bookingFilter = it },
                    onTableClick = { currentScreen = Screen.Bestallning }
                )
            } else {
                BestallningScreen(
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it }
                )
            }
        }
    }
}

@Composable
fun BordsOversiktScreen(
    bookingFilter: String,
    onFilterChange: (String) -> Unit,
    onTableClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(number = "12", label = "Totalt antal bord", modifier = Modifier.weight(1f))
            SummaryCard(number = "6", label = "Lediga bord", modifier = Modifier.weight(1f))
            SummaryCard(number = "4", label = "Upptagna bord", modifier = Modifier.weight(1f))
            SummaryCard(number = "2", label = "Needs attention", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Two-pane tablet layout: Left = Table Grid (Huvudsalen), Right = Bookings
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Pane: Huvudsalen Table Grid
            Card(
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Huvudsalen",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D2421)
                            )
                            Text(
                                text = "Välj ett ledigt bord för att placera en bokning",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "12 BORD · 42 PLATSER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val tables = listOf(
                        TableInfo("Bord 01", "2 platser", "LEDIGT", TableStatus.Ledig),
                        TableInfo("Bord 02", "4 platser", "UPPTAGET", TableStatus.Upptagen),
                        TableInfo("Bord 03", "2 platser", "LEDIGT", TableStatus.Ledig),
                        TableInfo("Bord 04", "4 platser", "NEEDS ATTENTION", TableStatus.Attention),
                        TableInfo("Bord 05", "4 platser", "UPPTAGET", TableStatus.Upptagen),
                        TableInfo("Bord 06", "2 platser", "LEDIGT", TableStatus.Ledig),
                        TableInfo("Bord 07", "6 platser", "UPPTAGET", TableStatus.Upptagen),
                        TableInfo("Bord 08", "4 platser", "LEDIGT", TableStatus.Ledig),
                        TableInfo("Bord 09", "2 platser", "LEDIGT", TableStatus.Ledig),
                        TableInfo("Bord 10", "4 platser", "NEEDS ATTENTION", TableStatus.Attention),
                        TableInfo("Bord 11", "4 platser", "UPPTAGET", TableStatus.Upptagen),
                        TableInfo("Bord 12", "6 platser", "LEDIGT", TableStatus.Ledig),
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(tables) { table ->
                            TableCard(table = table, onClick = onTableClick)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFFCBE0D4), label = "Ledigt")
                        LegendItem(color = Color(0xFFE5E5E0), label = "Upptaget")
                        LegendItem(color = Color(0xFFF3E7C4), label = "Needs attention")
                    }
                }
            }

            // Right Pane: Bookings & Selected Booking
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bookings list Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Dagens bokningar",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D2421)
                                )
                                Text(
                                    text = "Sorterade efter ankomsttid",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = "4 ST",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterPill(
                                text = "Alla",
                                selected = bookingFilter == "Alla",
                                onClick = { onFilterChange("Alla") }
                            )
                            FilterPill(
                                text = "Ej placerad",
                                selected = bookingFilter == "Ej placerad",
                                onClick = { onFilterChange("Ej placerad") }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val bookings = listOf(
                            BookingItem("Anna Andersson", "17:30 · 2 gäster", "Ej placerad"),
                            BookingItem("Erik Johansson", "18:00 · 4 gäster", "Bord 05"),
                            BookingItem("Sara Nilsson", "18:30 · 6 gäster", "Ej placerad"),
                            BookingItem("Johan Svensson", "19:00 · 3 gäster", "Bord 08")
                        )

                        val filteredBookings = if (bookingFilter == "Ej placerad") {
                            bookings.filter { it.status == "Ej placerad" }
                        } else {
                            bookings
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredBookings) { booking ->
                                BookingRow(booking = booking)
                            }
                        }
                    }
                }

                // Selected Booking Card (Vald bokning)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "VALD BOKNING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Anna Andersson",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2421)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "17:30 · 2 gäster · 60 minuters sittning",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Välj ett ledigt bord i planlösningen.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1D2421)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* No-op as requested */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F3E)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = "Tilldela bord", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

enum class TableStatus { Ledig, Upptagen, Attention }

data class TableInfo(
    val name: String,
    val seats: String,
    val statusText: String,
    val status: TableStatus
)

@Composable
fun TableCard(table: TableInfo, onClick: () -> Unit) {
    val bgColor = when (table.status) {
        TableStatus.Ledig -> Color(0xFFE8F2EC)
        TableStatus.Upptagen -> Color(0xFFEFEEEA)
        TableStatus.Attention -> Color(0xFFF9F3DC)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = table.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1D2421)
            )
            Column {
                Text(
                    text = table.seats,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = table.statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2421)
                )
            }
        }
    }
}

@Composable
fun SummaryCard(number: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = number,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2421)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

data class BookingItem(
    val name: String,
    val timeAndGuests: String,
    val status: String
)

@Composable
fun BookingRow(booking: BookingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEFEEEA), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = booking.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1D2421)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = booking.timeAndGuests,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = booking.status,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1D2421)
        )
    }
}

@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFF384E42) else Color(0xFFEFEEEA),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFF1D2421)
        )
    }
}

@Composable
fun BestallningScreen(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val categories = listOf("Dryck", "Förrätt", "Varmrätt", "Efterrätt")

    Column(modifier = Modifier.fillMaxSize()) {
        // Subtitle & Header prompt
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCategory == "Dryck") "Börja med något att dricka" else "Vad blir du sugen på?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2421)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedCategory == "Dryck") "Beställ i din takt — alla kategorier är alltid öppna." else "Hoppa över förrätten och välj varmrätt direkt.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tabs with indicator lines
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            categories.forEach { cat ->
                Column(
                    modifier = Modifier.clickable { onCategoryChange(cat) },
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = cat,
                        fontSize = 14.sp,
                        fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedCategory == cat) Color(0xFF1D2421) else Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(3.dp)
                            .background(if (selectedCategory == cat) Color(0xFF384E42) else Color.Transparent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category Pills
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { cat ->
                FilterPill(
                    text = cat,
                    selected = selectedCategory == cat,
                    onClick = { onCategoryChange(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (selectedCategory == "Dryck") "Alla kategorier är öppna – du behöver inte följa stegen i ordning." else "Förrätt är valfri. Alla menyval är tillgängliga hela tiden.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Two pane or wide layout for menu items and order summary
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Menu Items List (Left / Main area)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                val itemsList = if (selectedCategory == "Dryck") {
                    listOf(
                        MenuItemData("AS", "Husets lemonad", "Citron · mynta · sodavatten", "45 kr"),
                        MenuItemData("AS", "Mineralvatten", "Naturellt eller kolsyrat", "35 kr"),
                        MenuItemData("AS", "Äppelmust", "Från svenska äpplen", "42 kr")
                    )
                } else {
                    listOf(
                        MenuItemData("AS", "Smörstekt torsk", "Potatispuré · brynt smör · ärtor", "219 kr"),
                        MenuItemData("AS", "Svamprisotto", "Kantareller · parmesan · örter", "195 kr"),
                        MenuItemData("AS", "Långbakad högrev", "Rödvinssky · rostad potatis", "229 kr")
                    )
                }

                items(itemsList) { item ->
                    MenuItemRow(item = item)
                }
            }

            // Order Summary Card (Right area)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Din beställning",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D2421)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (selectedCategory == "Dryck") "1 dryck i varukorgen" else "1 dryck + 1 varmrätt",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                text = if (selectedCategory == "Dryck") "45 kr" else "264 kr",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D2421)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { /* No-op as requested */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F3E)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = "Visa beställning", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { /* No-op as requested */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F4F3E)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = if (selectedCategory == "Dryck") "Lägg beställning" else "Skicka till köket",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MenuItemData(
    val badge: String,
    val title: String,
    val description: String,
    val price: String
)

@Composable
fun MenuItemRow(item: MenuItemData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFEEEA),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.badge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1D2421)
                        )
                    }
                }
                Column {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1D2421)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = item.price,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1D2421)
            )
        }
    }
}
