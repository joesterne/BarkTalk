package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TranslationLog
import com.example.util.BarkHistoryPdfExporter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog that previews and exports recorded bark translation history logs into
 * a shareable PDF document (via FileProvider ShareSheet or SAF CreateDocument).
 */
@Composable
fun PdfExportDialog(
    logs: List<TranslationLog>,
    onDismissRequest: () -> Unit,
    onPdfGenerated: ((java.io.File) -> Unit)? = null
) {
    val context = LocalContext.current
    var reportTitle by remember {
        mutableStateOf("BarkTalk Canine Translation History Report")
    }
    var lastExportedFileName by remember { mutableStateOf<String?>(null) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    BarkHistoryPdfExporter.writePdfToStream(logs, outStream, reportTitle)
                }
                Toast.makeText(
                    context,
                    "PDF saved successfully! (${logs.size} translations)",
                    Toast.LENGTH_SHORT
                ).show()
                onDismissRequest()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed saving PDF: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val avgDb = remember(logs) {
        if (logs.isNotEmpty()) logs.map { it.intensityDb }.average().toInt() else 0
    }
    val avgPitch = remember(logs) {
        if (logs.isNotEmpty()) logs.map { it.dominantPitchHz }.average().toInt() else 0
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag("pdf_export_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "Export History to PDF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shareable A4 report with timestamps & AI translations",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = reportTitle,
                    onValueChange = { reportTitle = it },
                    label = { Text("PDF Report Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_report_title_input")
                )

                // Report Preview Summary Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "PDF Contents Summary",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "• Recorded Bark Logs Included: ${logs.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Acoustic Metrics: Avg $avgDb dB • Avg $avgPitch Hz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Fields: Timestamp, Dog Profile, AI English Translation, Inner Thought & Urgency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                lastExportedFileName?.let { fileName ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_generated_status_banner")
                    ) {
                        Text(
                            text = "✓ Generated $fileName",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Action Buttons inside dialog for Share vs Save to Device
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val pdfFile = BarkHistoryPdfExporter.exportAndShareLogs(
                                context = context,
                                logs = logs,
                                reportTitle = reportTitle.ifBlank { "BarkTalk Canine Translation History Report" }
                            )
                            lastExportedFileName = pdfFile.name
                            onPdfGenerated?.invoke(pdfFile)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_share_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Generate & Share PDF")
                    }

                    OutlinedButton(
                        onClick = {
                            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            savePdfLauncher.launch("BarkTalk_History_$dateStamp.pdf")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_pdf_to_device_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Save PDF to Device")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("close_pdf_dialog_button")
            ) {
                Text("Close")
            }
        }
    )
}
