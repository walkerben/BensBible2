package com.bensbible.app.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bensbible.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Holds the action to run once the user grants the notification permission.
    var pendingEnableAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) pendingEnableAction?.invoke()
        pendingEnableAction = null
    }

    // Call this before enabling any notification feature.
    fun withNotificationPermission(context: Context, onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingEnableAction = onGranted
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onGranted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Verse of the Day") },
                supportingContent = { Text("Receive a daily scripture verse as a notification") },
                trailingContent = {
                    Switch(
                        checked = viewModel.isVerseOfTheDayEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                withNotificationPermission(context) {
                                    viewModel.setVerseOfTheDayEnabled(true, context)
                                }
                            } else {
                                viewModel.setVerseOfTheDayEnabled(false, context)
                            }
                        }
                    )
                }
            )

            if (viewModel.isVerseOfTheDayEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Notification Time") },
                    supportingContent = {
                        Text(formatTime(viewModel.notificationHour, viewModel.notificationMinute))
                    },
                    leadingContent = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    trailingContent = {
                        TextButton(onClick = {
                            showTimePicker(
                                context = context,
                                hour = viewModel.notificationHour,
                                minute = viewModel.notificationMinute
                            ) { h, m ->
                                viewModel.setNotificationTime(h, m, context)
                            }
                        }) {
                            Text("Change")
                        }
                    }
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the notification to open the verse in the reader.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Memorization",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Memorization Reminder") },
                supportingContent = { Text("Daily reminder to review your memorization verses") },
                trailingContent = {
                    Switch(
                        checked = viewModel.isMemorizeReminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                withNotificationPermission(context) {
                                    viewModel.setMemorizeReminderEnabled(true, context)
                                }
                            } else {
                                viewModel.setMemorizeReminderEnabled(false, context)
                            }
                        }
                    )
                }
            )

            if (viewModel.isMemorizeReminderEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Reminder Time") },
                    supportingContent = {
                        Text(formatTime(viewModel.memorizeReminderHour, viewModel.memorizeReminderMinute))
                    },
                    leadingContent = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    trailingContent = {
                        TextButton(onClick = {
                            showTimePicker(
                                context = context,
                                hour = viewModel.memorizeReminderHour,
                                minute = viewModel.memorizeReminderMinute
                            ) { h, m ->
                                viewModel.setMemorizeReminderTime(h, m, context)
                            }
                        }) {
                            Text("Change")
                        }
                    }
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only sent when you have verses due for review.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Daily Reading",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Daily Reading Reminder") },
                supportingContent = { Text("Daily reminder for your Bible reading plan") },
                trailingContent = {
                    Switch(
                        checked = viewModel.isReadingPlanReminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                withNotificationPermission(context) {
                                    viewModel.setReadingPlanReminderEnabled(true, context)
                                }
                            } else {
                                viewModel.setReadingPlanReminderEnabled(false, context)
                            }
                        }
                    )
                }
            )

            if (viewModel.isReadingPlanReminderEnabled) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Reminder Time") },
                    supportingContent = {
                        Text(formatTime(viewModel.readingPlanReminderHour, viewModel.readingPlanReminderMinute))
                    },
                    leadingContent = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    trailingContent = {
                        TextButton(onClick = {
                            showTimePicker(
                                context = context,
                                hour = viewModel.readingPlanReminderHour,
                                minute = viewModel.readingPlanReminderMinute
                            ) { h, m ->
                                viewModel.setReadingPlanReminderTime(h, m, context)
                            }
                        }) {
                            Text("Change")
                        }
                    }
                )
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Only sent when you have an active reading plan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, amPm)
}

private fun showTimePicker(
    context: Context,
    hour: Int,
    minute: Int,
    onTimeSet: (Int, Int) -> Unit
) {
    TimePickerDialog(context, { _, h, m -> onTimeSet(h, m) }, hour, minute, false).show()
}
