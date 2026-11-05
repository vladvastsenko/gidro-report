package com.example.gidroreport

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    data class PhotoItem(
        val uri: Uri,
        val comment: MutableState<String>
    )

    data class CustomField(
        val name: MutableState<String>,
        val value: MutableState<String>
    )

    private val photos = mutableStateListOf<PhotoItem>()
    private val customFields = mutableStateListOf<CustomField>()

    private var logoUri: Uri? = null
    private var pendingCameraUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                photos.add(PhotoItem(uri, mutableStateOf("")))
            }
        }
        pendingCameraUri = null
    }

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photos.add(PhotoItem(uri, mutableStateOf("")))
        }
    }

    private val pickLogo = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            logoUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val savedLanguage = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("language", "")

        if (!savedLanguage.isNullOrBlank()) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(savedLanguage)
            )
        }

        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ReportScreen()
            }
        }
    }

    private fun applyLanguage(languageTag: String) {
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("language", languageTag)
            .apply()

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    private fun launchCamera() {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "GidroReport_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        if (uri != null) {
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ReportScreen() {
        var objectName by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var area by remember { mutableStateOf("") }
        var waterproofingType by remember { mutableStateOf("") }
        var inspector by remember { mutableStateOf("") }
        var contractor by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        Scaffold(
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.app_name)) })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(stringResource(R.string.language), fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { applyLanguage("fi") },
                            label = { Text("Suomi") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { applyLanguage("ru") },
                            label = { Text("Русский") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { applyLanguage("en") },
                            label = { Text("English") }
                        )
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            pickLogo.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.choose_logo))
                    }

                    if (logoUri != null) {
                        Text(stringResource(R.string.logo_selected))
                    }
                }

                item {
                    OutlinedTextField(
                        value = objectName,
                        onValueChange = { objectName = it },
                        label = { Text(stringResource(R.string.object_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(stringResource(R.string.address)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text(stringResource(R.string.area)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = waterproofingType,
                        onValueChange = { waterproofingType = it },
                        label = { Text(stringResource(R.string.waterproofing_type)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = inspector,
                        onValueChange = { inspector = it },
                        label = { Text(stringResource(R.string.inspector)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = contractor,
                        onValueChange = { contractor = it },
                        label = { Text(stringResource(R.string.contractor)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(stringResource(R.string.inspection_result), fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            stringResource(R.string.result_compliant),
                            stringResource(R.string.result_with_comments),
                            stringResource(R.string.result_non_compliant)
                        ).forEach { option ->
                            FilterChip(
                                selected = result == option,
                                onClick = { result = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(stringResource(R.string.notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }

                item {
                    Text(stringResource(R.string.custom_fields), fontSize = 18.sp)
                }

                items(customFields) { field ->
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = field.name.value,
                                onValueChange = { field.name.value = it },
                                label = { Text(stringResource(R.string.custom_field_name)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = field.value.value,
                                onValueChange = { field.value.value = it },
                                label = { Text(stringResource(R.string.custom_field_value)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedButton(
                                onClick = { customFields.remove(field) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            customFields.add(
                                CustomField(
                                    name = mutableStateOf(""),
                                    value = mutableStateOf("")
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add_custom_field))
                    }
                }

                item {
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.take_photo))
                    }
                }

                item {
                    Button(
                        onClick = {
                            pickMedia.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.choose_photo))
                    }
                }

                items(photos) { photo ->
                    PhotoCard(photo)
                }

                item {
                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = getString(R.string.creating_pdf)
                                val uri = createPdf(
                                    objectName = objectName,
                                    address = address,
                                    area = area,
                                    waterproofingType = waterproofingType,
                                    inspector = inspector,
                                    contractor = contractor,
                                    result = result,
                                    notes = notes,
                                    dateText = dateFormat.format(Date())
                                )

                                statusMessage = if (uri != null) {
                                    sharePdf(uri)
                                    getString(R.string.pdf_created)
                                } else {
                                    getString(R.string.pdf_failed)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.create_pdf))
                    }
                }

                item {
                    if (statusMessage.isNotEmpty()) {
                        Text(statusMessage)
                    }
                }
            }
        }
    }

    @Composable
    fun PhotoCard(photo: PhotoItem) {
        val bitmap = remember(photo.uri) {
            loadBitmap(photo.uri, 900)
        }

        Card {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = stringResource(R.string.pdf_photo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = photo.comment.value,
                    onValueChange = { photo.comment.value = it },
                    label = { Text(stringResource(R.string.photo_comment)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    private fun loadBitmap(uri: Uri, maxSize: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            var sampleSize = 1
            while (
                bounds.outWidth / sampleSize > maxSize ||
                bounds.outHeight / sampleSize > maxSize
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun createPdf(
        objectName: String,
        address: String,
        area: String,
        waterproofingType: String,
        inspector: String,
        contractor: String,
        result: String,
        notes: String,
        dateText: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            var canvas = page.canvas
            var y = 50f

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
            }

            val commentPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
            }

            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            fun nextPage() {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                )
                canvas = page.canvas
                y = 50f
            }

            fun addText(label: String, value: String) {
                val text = "$label: ${value.ifBlank { "—" }}"
                val layout = StaticLayout.Builder
                    .obtain(text, 0, text.length, textPaint, pageWidth - 100)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(2f, 1f)
                    .build()

                if (y + layout.height > pageHeight - 60) {
                    nextPage()
                }

                canvas.drawText(label, 50f, y, labelPaint)
                y += 14f

                canvas.save()
                canvas.translate(50f, y)
                layout.draw(canvas)
                canvas.restore()

                y += layout.height + 8f
            }

            logoUri?.let { uri ->
                loadBitmap(uri, 400)?.let { logo ->
                    val maxLogoWidth = 120f
                    val maxLogoHeight = 60f
                    val scale = minOf(
                        maxLogoWidth / logo.width,
                        maxLogoHeight / logo.height
                    )

                    val logoWidth = logo.width * scale
                    val logoHeight = logo.height * scale

                    val scaledLogo = Bitmap.createScaledBitmap(
                        logo,
                        logoWidth.toInt().coerceAtLeast(1),
                        logoHeight.toInt().coerceAtLeast(1),
                        true
                    )

                    canvas.drawBitmap(
                        scaledLogo,
                        pageWidth - 50f - logoWidth,
                        40f,
                        null
                    )
                }
            }

            canvas.drawText(getString(R.string.pdf_title), 50f, y, titlePaint)
            y += 30f

            addText(getString(R.string.pdf_date), dateText)
            addText(getString(R.string.pdf_object), objectName)
            addText(getString(R.string.pdf_address), address)
            addText(getString(R.string.pdf_area), area)
            addText(getString(R.string.pdf_type), waterproofingType)
            addText(getString(R.string.pdf_inspector), inspector)
            addText(getString(R.string.pdf_contractor), contractor)
            addText(getString(R.string.pdf_result), result)
            addText(getString(R.string.pdf_notes), notes)

            if (customFields.isNotEmpty()) {
                y += 8f
                canvas.drawText(getString(R.string.pdf_custom_fields), 50f, y, titlePaint)
                y += 25f

                customFields.forEach { field ->
                    addText(
                        field.name.value.ifBlank { "—" },
                        field.value.value
                    )
                }
            }

            photos.chunked(4).forEach { photoChunk ->
                nextPage()

                canvas.drawText(getString(R.string.pdf_photos), 50f, y, titlePaint)
                y += 25f

                val margin = 50f
                val gap = 15f
                val cellWidth = (pageWidth - margin * 2 - gap) / 2f
                val cellHeight = 220f
                val commentHeight = 40f
                val rowHeight = cellHeight + commentHeight + gap

                photoChunk.forEachIndexed { indexInChunk, photo ->
                    val globalIndex = photos.indexOf(photo)
                    val column = indexInChunk % 2
                    val row = indexInChunk / 2

                    val x = margin + column * (cellWidth + gap)
                    val imageTop = y + row * rowHeight

                    canvas.drawText(
                        "${getString(R.string.pdf_photo)} ${globalIndex + 1}",
                        x,
                        imageTop,
                        labelPaint
                    )

                    loadBitmap(photo.uri, 1200)?.let { bitmap ->
                        val scale = minOf(
                            cellWidth / bitmap.width,
                            cellHeight / bitmap.height
                        )

                        val drawWidth = bitmap.width * scale
                        val drawHeight = bitmap.height * scale
                        val drawX = x + (cellWidth - drawWidth) / 2f
                        val drawY = imageTop + 14f + (cellHeight - drawHeight) / 2f

                        val scaled = Bitmap.createScaledBitmap(
                            bitmap,
                            drawWidth.toInt().coerceAtLeast(1),
                            drawHeight.toInt().coerceAtLeast(1),
                            true
                        )

                        canvas.drawBitmap(scaled, drawX, drawY, null)
                    }

                    val comment = photo.comment.value
                        .ifBlank { "—" }
                        .take(80)

                    canvas.drawText(
                        comment,
                        x,
                        imageTop + cellHeight + 24f,
                        commentPaint
                    )
                }
            }

            document.finishPage(page)

            val fileName = "GidroReport_${SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())}.pdf"

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            }

            val uri = contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@withContext null

            contentResolver.openOutputStream(uri)?.use { stream ->
                document.writeTo(stream)
            }

            document.close()
            uri
        } catch (e: Exception) {
            null
        }
    }

    private fun sharePdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.share_pdf)))
    }
}
