package com.example.hexaward.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexaward.data.update.DownloadState
import com.example.hexaward.data.update.UpdateInfo
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = hiltViewModel()
    val downloadState by viewModel.downloadState.collectAsState()
    
    // Don't allow dismissing during download
    val canDismiss = downloadState !is DownloadState.Downloading
    
    AlertDialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        icon = {
            Icon(
                Icons.Default.NewReleases,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (downloadState is DownloadState.Downloading) "Downloading Update..." else "Update Available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Version info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Version",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = updateInfo.currentVersion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "New Version",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = updateInfo.latestVersion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Release notes
                Text(
                    text = "What's New:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = updateInfo.releaseNotes.ifEmpty { "Bug fixes and improvements" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 20.sp
                    )
                }
                
                // Download progress
                when (downloadState) {
                    is DownloadState.Downloading -> {
                        Spacer(Modifier.height(16.dp))
                        Column {
                            LinearProgressIndicator(
                                progress = (downloadState as DownloadState.Downloading).progress / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "${(downloadState as DownloadState.Downloading).progress}% downloaded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is DownloadState.Downloaded -> {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "✓ Download complete! Installing...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        // Auto-close dialog after installation starts
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(1000)
                            onDismiss()
                        }
                    }
                    is DownloadState.Error -> {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Error: ${(downloadState as DownloadState.Error).message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.startDownload(updateInfo.downloadUrl, updateInfo.latestVersion)
                },
                enabled = downloadState !is DownloadState.Downloading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (downloadState is DownloadState.Downloading) "Downloading..." else "Download Update")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = downloadState !is DownloadState.Downloading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Later")
            }
        }
    )
}
