package com.nilay.budgetbuddy.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nilay.budgetbuddy.ui.settings.SettingsViewModel
import com.nilay.budgetbuddy.ui.theme.GreenAccent
import com.nilay.budgetbuddy.ui.theme.OneUiBlue
import com.nilay.budgetbuddy.ui.theme.OrangeAccent
import com.nilay.budgetbuddy.ui.theme.PillShape
import com.nilay.budgetbuddy.ui.theme.PurpleAccent
import com.nilay.budgetbuddy.ui.theme.RedAccent
import com.nilay.budgetbuddy.util.CurrencyFormatter
import android.widget.Toast
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onManageCategories: () -> Unit
) {
    val darkModeSetting by viewModel.darkModeFlow.collectAsState()
    val currency by viewModel.currencyFlow.collectAsState()
    val colorScheme by viewModel.colorSchemeFlow.collectAsState()
    val userName by viewModel.userNameFlow.collectAsState()
    val userEmail by viewModel.userEmailFlow.collectAsState()
    val profilePicturePath by viewModel.profilePicturePathFlow.collectAsState()
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showColorSchemeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AccountCard(
                userName = userName,
                userEmail = userEmail,
                profilePicturePath = profilePicturePath,
                onProfilePictureSelected = viewModel::updateProfilePicture,
                onLogout = { showLogoutDialog = true }
            )

            SettingsSection(title = "Preferences") {
                SettingsToggleItem(
                    title = "Dark Mode",
                    icon = Icons.Rounded.DarkMode,
                    checked = darkModeSetting ?: false,
                    onCheckedChange = viewModel::toggleDarkMode
                )
                SettingsActionItem(
                    title = "Currency",
                    icon = Icons.Rounded.CurrencyExchange,
                    trailingText = currency,
                    onClick = { showCurrencyDialog = true }
                )
                SettingsActionItem(
                    title = "Color Scheme",
                    icon = Icons.Rounded.Palette,
                    trailingText = colorSchemeLabel(colorScheme),
                    onClick = { showColorSchemeDialog = true }
                )
            }

            SettingsSection(title = "Data Management") {
                SettingsActionItem(
                    title = "Manage Categories",
                    icon = Icons.Rounded.Category,
                    onClick = onManageCategories
                )
                SettingsActionItem(
                    title = "Export to CSV",
                    icon = Icons.Rounded.FileDownload,
                    onClick = {
                        viewModel.exportToCsv()
                        Toast.makeText(context, "CSV Export started...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            SettingsSection(title = "About") {
                SettingsActionItem(
                    title = "Version",
                    icon = Icons.Rounded.Info,
                    trailingText = "1.0.0",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            selected = currency,
            onSelect = { code ->
                viewModel.updateCurrency(code)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showColorSchemeDialog) {
        ColorSchemePickerDialog(
            selected = colorScheme,
            onSelect = { scheme ->
                viewModel.updateColorScheme(scheme)
                showColorSchemeDialog = false
            },
            onDismiss = { showColorSchemeDialog = false }
        )
    }

    if (showLogoutDialog) {
        SleekAlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to log back in to see your data.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) { Text("Log Out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/** Flat surface + One UI-sized corners, instead of AlertDialog's default tonally-tinted container. */
@Composable
private fun SleekAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}

@Composable
fun AccountCard(
    userName: String?,
    userEmail: String?,
    profilePicturePath: String?,
    onProfilePictureSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) { copyToProfilePictureFile(context, uri) }
                if (path != null) onProfilePictureSelected(path)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicturePath != null) {
                    AsyncImage(
                        model = profilePicturePath,
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = (userName?.trim()?.firstOrNull()?.uppercase() ?: "?"),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = "Log out",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CurrencyPickerDialog(selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    SleekAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Currency") },
        text = {
            Column {
                CurrencyFormatter.supportedCurrencies.forEach { code ->
                    val isSelected = code == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onSelect(code) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$code (${CurrencyFormatter.format(0.0, code)})")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

private fun copyToProfilePictureFile(context: Context, uri: Uri): String? = try {
    val file = File(context.filesDir, "profile_picture.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    file.absolutePath
} catch (e: Exception) {
    null
}

private data class ColorSchemeOption(val key: String, val label: String, val swatch: Color?)

private val colorSchemeOptions = listOf(
    ColorSchemeOption("DYNAMIC", "Dynamic (Wallpaper)", null),
    ColorSchemeOption("BLUE", "Blue", OneUiBlue),
    ColorSchemeOption("GREEN", "Green", GreenAccent),
    ColorSchemeOption("PURPLE", "Purple", PurpleAccent),
    ColorSchemeOption("ORANGE", "Orange", OrangeAccent),
    ColorSchemeOption("RED", "Red", RedAccent)
)

private fun colorSchemeLabel(key: String): String = colorSchemeOptions.find { it.key == key }?.label ?: "Dynamic (Wallpaper)"

@Composable
fun ColorSchemePickerDialog(selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    SleekAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Color Scheme") },
        text = {
            Column {
                colorSchemeOptions.forEach { option ->
                    val isSelected = option.key == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                            .clickable { onSelect(option.key) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onSelect(option.key) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(option.swatch ?: MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (option.swatch == null) {
                                Icon(
                                    Icons.Rounded.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(option.label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun SettingsActionItem(
    title: String,
    icon: ImageVector,
    trailingText: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}
