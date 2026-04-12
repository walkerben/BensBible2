package com.bensbible.app.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bensbible.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                        onCheckedChange = { viewModel.setVerseOfTheDayEnabled(it, context) }
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
                        onCheckedChange = { viewModel.setMemorizeReminderEnabled(it, context) }
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
