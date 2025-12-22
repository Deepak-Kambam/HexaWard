package com.example.hexaward.data.monitor

import android.content.Context
import android.os.FileObserver
import com.example.hexaward.domain.model.SecuritySignal
import com.example.hexaward.domain.model.SignalSeverity
import com.example.hexaward.domain.model.SignalSource
import com.example.hexaward.domain.model.SignalType
import com.example.hexaward.domain.monitor.SignalMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates hidden decoy files in common directories.
 * If these files are touched by any process, it's a critical indicator of ransomware.
 */
@Singleton
class HoneyfileMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : SignalMonitor {

    private val _signals = MutableSharedFlow<SecuritySignal>()
    override val signals: SharedFlow<SecuritySignal> = _signals.asSharedFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)
    private var fileObserver: FileObserver? = null

    private val honeyFiles = listOf(
        File(context.getExternalFilesDir(null), ".000_honey_decoy.txt"),
        File(context.getExternalFilesDir(null), ".aaa_important_metadata.dat")
    )

    override fun startMonitoring() {
        createHoneyFiles()
        setupObserver()
    }

    override fun stopMonitoring() {
        fileObserver?.stopWatching()
    }

    private fun createHoneyFiles() {
        honeyFiles.forEach { file ->
            if (!file.exists()) {
                try {
                    file.createNewFile()
                    file.writeText("THIS IS A SYSTEM PROTECTION FILE. DO NOT MODIFY.")
                } catch (e: Exception) {
                    // Handle storage permission issues
                }
            }
        }
    }

    private fun setupObserver() {
        val path = context.getExternalFilesDir(null)?.absolutePath ?: return
        
        // We observe the directory containing our honeyfiles
        fileObserver = object : FileObserver(path, MODIFY or DELETE or MOVED_FROM) {
            override fun onEvent(event: Int, fileName: String?) {
                if (fileName == null) return
                
                // Check if the event happened to one of our honeyfiles
                if (honeyFiles.any { it.name == fileName }) {
                    scope.launch {
                        _signals.emit(
                            SecuritySignal(
                                source = SignalSource.HONEYFILE,
                                type = SignalType.HONEYFILE_TOUCHED,
                                severity = SignalSeverity.CRITICAL,
                                description = "CRITICAL: Honeyfile decoy '$fileName' was modified or deleted!",
                                metadata = mapOf("file" to fileName, "event" to event.toString())
                            )
                        )
                    }
                }
            }
        }
        fileObserver?.startWatching()
    }
}
