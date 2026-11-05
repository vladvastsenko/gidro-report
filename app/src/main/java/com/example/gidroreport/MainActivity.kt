package com.example.gidroreport

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var logoUri by mutableStateOf<Uri?>(null)
    private var currentReport: ReportData? = null

    private val logoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            logoUri = uri
            Toast.makeText(this, R.string.logo_updated, Toast.LENGTH_SHORT).show()
        }
    }

    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val report = currentReport
            if (report != null) {
                report.photos.add(uri)
                Toast.makeText(this, R.string.photo_added, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ReportScreen(
                    logoUri = logoUri,
                    onPickLogo = { logoPicker.launch("image/*") },
                    onPickPhoto = {
                        currentReport = it
                        photoPicker.launch("image/*")
                    },
                    onExportPdf = { report -> exportPdf(report) }
                )
            }
        }
    }

    private fun exportPdf(report: ReportData) {
        try {
            val context = this
            val documentsDir = File(context.getExternalFilesDir(null), "reports")
            if (!documentsDir.exists()) documentsDir.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val pdfFile = File(documentsDir, "Valvontapoytakirja_$timeStamp.pdf")

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                isFakeBoldText = true
            }
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }
            val smallPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9f
            }

            var y = 50f

            canvas.drawText("Valvontapöytäkirja", 40f, y, titlePaint)
            y += 30f

            canvas.drawText("Kohde: ${report.objectName}", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Osoite: ${report.address}", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Päivämäärä: ${report.date}", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Valvoja: ${report.inspector}", 40f, y, textPaint)
            y += 28f

            canvas.drawText("Työn kuvaus: ${report.workDescription}", 40f, y, textPaint)
            y += 28f

            canvas.drawText("Ohjeet luettu: ${report.instructionsRead}", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Seinän paksuus: ${report.wallThickness} mm", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Seinän tulos: ${report.wallThicknessResult}", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Lattian paksuus: ${report.floorThickness} mm", 40f, y, textPaint)
            y += 18f
            canvas.drawText("Lattian tulos: ${report.floorThicknessResult}", 40f, y, textPaint)
            y += 28f

            canvas.drawText("Lisätiedot:", 40f, y, textPaint)
            y += 18f
            canvas.drawText(report.notes, 40f, y, smallPaint)
            y += 28f

            canvas.drawText("Tulos: ${report.result}", 40f, y, titlePaint)
            y += 40f

            val photosPerPage = 4
            val photoSize = 220f
            var photoIndex = 0

            for (photoUri in report.photos) {
                if (photoIndex > 0 && photoIndex % photosPerPage == 0) {
                    document.finishPage(page)
                    val newPage = document.startPage(pageInfo)
                    canvas.set(newPage.canvas)
                    y = 50f
                }

                try {
                    val bitmap = decodeScaledBitmap(context, photoUri, 800)
                    if (bitmap != null) {
                        val left = 40f
                        val top = y
                        canvas.drawBitmap(
                            bitmap,
                            null,
                            android.graphics.RectF(left, top, left + photoSize, top + photoSize),
                            null
                        )
                        y += photoSize + 20f
                    }
                } catch (_: Exception) {
                }

                photoIndex++
            }

            document.finishPage(page)

            FileOutputStream(pdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))

        } catch (e: Exception) {
            Toast.makeText(this, "PDF error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun decodeScaledBitmap(context: android.content.Context, uri: Uri, maxSide: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var sampleSize = 1
            while (options.outWidth / sampleSize > maxSide || options.outHeight / sampleSize > maxSide) {
                sampleSize *= 2
            }

            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, loadOptions)
            }
        } catch (_: Exception) {
            null
        }
    }
}

data class ReportData(
    var objectName: String = "",
    var address: String = "",
    var date: String = "",
    var inspector: String = "",
    var workDescription: String = "",
    var instructionsRead: String = "",
    var wallThickness: String = "",
    var wallThicknessResult: String = "",
    var floorThickness: String = "",
    var floorThicknessResult: String = "",
    var notes: String = "",
    var result: String = "",
    val photos: MutableList<Uri> = mutableListOf()
)

@Composable
fun ReportScreen(
    logoUri: Uri?,
    onPickLogo: () -> Unit,
    onPickPhoto: (ReportData) -> Unit,
    onExportPdf: (ReportData) -> Unit
) {
    val context = LocalContext.current

    var report by remember { mutableStateOf(ReportData()) }
    var statusMessage by remember { mutableStateOf("") }

    val yesText = stringResource(R.string.yes)
    val noText = stringResource(R.string.no)
    val meetsText = stringResource(R.string.meets_requirements)
    val notMeetsText = stringResource(R.string.does_not_meet_requirements)
    val approvedText = stringResource(R.string.result_approved)
    val rejectedText = stringResource(R.string.result_rejected)

    var showNotesDialog by remember { mutableStateOf(false) }
    var notesDraft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { onPickLogo() },
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    val bitmap = remember(logoUri) {
                        context.contentResolver.openInputStream(logoUri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.add_logo),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.app_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = report.objectName,
            onValueChange = { report = report.copy(objectName = it) },
            label = { Text(stringResource(R.string.object_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = report.address,
            onValueChange = { report = report.copy(address = it) },
            label = { Text(stringResource(R.string.address)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = report.date,
            onValueChange = { report = report.copy(date = it) },
            label = { Text(stringResource(R.string.date)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = report.inspector,
            onValueChange = { report = report.copy(inspector = it) },
            label = { Text(stringResource(R.string.inspector)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = report.workDescription,
            onValueChange = { report = report.copy(workDescription = it) },
            label = { Text(stringResource(R.string.work_description)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.instructions_read))

        Row {
            FilterChip(
                selected = report.instructionsRead == yesText,
                onClick = { report = report.copy(instructionsRead = yesText) },
                label = { Text(yesText) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = report.instructionsRead == noText,
                onClick = { report = report.copy(instructionsRead = noText) },
                label = { Text(noText) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = report.wallThickness,
            onValueChange = { report = report.copy(wallThickness = it) },
            label = { Text(stringResource(R.string.wall_thickness)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            FilterChip(
                selected = report.wallThicknessResult == meetsText,
                onClick = { report = report.copy(wallThicknessResult = meetsText) },
                label = { Text(meetsText) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = report.wallThicknessResult == notMeetsText,
                onClick = { report = report.copy(wallThicknessResult = notMeetsText) },
                label = { Text(notMeetsText) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = report.floorThickness,
            onValueChange = { report = report.copy(floorThickness = it) },
            label = { Text(stringResource(R.string.floor_thickness)) },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
            FilterChip(
                selected = report.floorThicknessResult == meetsText,
                onClick = { report = report.copy(floorThicknessResult = meetsText) },
                label = { Text(meetsText) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = report.floorThicknessResult == notMeetsText,
                onClick = { report = report.copy(floorThicknessResult = notMeetsText) },
                label = { Text(notMeetsText) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                notesDraft = report.notes
                showNotesDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_notes))
        }

        if (report.notes.isNotBlank()) {
            Text(text = report.notes)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.result))

        Row {
            FilterChip(
                selected = report.result == approvedText,
                onClick = { report = report.copy(result = approvedText) },
                label = { Text(approvedText) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = report.result == rejectedText,
                onClick = { report = report.copy(result = rejectedText) },
                label = { Text(rejectedText) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPickPhoto(report) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_photo))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(stringResource(R.string.photos_count, report.photos.size))

        report.photos.forEachIndexed { index, uri ->
            val bitmap = remember(uri) {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(text = "${index + 1}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onExportPdf(report) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.export_pdf))
        }

        if (statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = statusMessage)
        }
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text(stringResource(R.string.add_notes)) },
            text = {
                OutlinedTextField(
                    value = notesDraft,
                    onValueChange = { notesDraft = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        report = report.copy(notes = notesDraft)
                        showNotesDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
