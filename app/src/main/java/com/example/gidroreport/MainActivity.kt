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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    data class PhotoItem(
        val uri: Uri,
        var comment: String
    )

    private val photos = mutableStateListOf<PhotoItem>()

    private var pendingCameraUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { uri ->
                photos.add(PhotoItem(uri, ""))
            }
        }
        pendingCameraUri = null
    }

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photos.add(PhotoItem(uri, ""))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                ReportScreen()
            }
        }
    }

    private fun launchCamera() {
        val values = ContentValues().apply {
            put(
                MediaStore.Images.DISPLAY_NAME,
                "GidroReport_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.MIME_TYPE, "image/jpeg")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.EXTERNAL_CONTENT_URI,
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
        var waterproofingType by remember { mutableStateOf("Обмазочная") }
        var inspector by remember { mutableStateOf("") }
        var contractor by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("Соответствует") }
        var statusMessage by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("ГидроОтчёт") })
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
                    OutlinedTextField(
                        value = objectName,
                        onValueChange = { objectName = it },
                        label = { Text("Объект") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Адрес") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Участок / ось / помещение") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = waterproofingType,
                        onValueChange = { waterproofingType = it },
                        label = { Text("Тип гидроизоляции") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = inspector,
                        onValueChange = { inspector = it },
                        label = { Text("Инспектор") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = contractor,
                        onValueChange = { contractor = it },
                        label = { Text("Подрядчик") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Результат осмотра", fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Соответствует",
                            "С замечаниями",
                            "Не соответствует"
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
                        label = { Text("Общие замечания и описание работ") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }

                item {
                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сделать фото")
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
                        Text("Добавить фото из галереи")
                    }
                }

                items(photos) { photo ->
                    PhotoCard(photo)
                }

                item {
                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = "Создаю PDF..."
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
                                    "PDF создан и открыт для отправки"
                                } else {
                                    "Не удалось создать PDF"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Создать и отправить PDF")
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
                        contentDescription = "Фото участка",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = photo.comment,
                    onValueChange = { photo.comment = it },
                    label = { Text("Комментарий к фото") },
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
                textSize = 20f
                isFakeBoldText = true
            }

            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
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
                y += 16f

                canvas.save()
                canvas.translate(50f, y)
                layout.draw(canvas)
                canvas.restore()

                y += layout.height + 10f
            }

            canvas.drawText("ОТЧЁТ О КОНТРОЛЕ ГИДРОИЗОЛЯЦИИ", 50f, y, titlePaint)
            y += 35f

            addText("Дата и время", dateText)
            addText("Объект", objectName)
            addText("Адрес", address)
            addText("Участок / ось / помещение", area)
            addText("Тип гидроизоляции", waterproofingType)
            addText("Инспектор", inspector)
            addText("Подрядчик", contractor)
            addText("Результат осмотра", result)
            addText("Замечания и описание работ", notes)

            y += 10f
            canvas.drawText("ФОТОМАТЕРИАЛЫ", 50f, y, titlePaint)
            y += 30f

            photos.forEachIndexed { index, photo ->
                val bitmap = loadBitmap(photo.uri, 1000)

                if (bitmap != null) {
                    val maxWidth = pageWidth - 100
                    val scale = maxWidth.toFloat() / bitmap.width
                    val imageHeight = bitmap.height * scale

                    if (y + imageHeight + 80 > pageHeight - 60) {
                        nextPage()
                    }

                    val scaled = Bitmap.createScaledBitmap(
                        bitmap,
                        maxWidth,
                        imageHeight.toInt(),
                        true
                    )

                    canvas.drawText("Фото ${index + 1}", 50f, y, labelPaint)
                    y += 16f

                    canvas.drawBitmap(scaled, 50f, y, null)
                    y += imageHeight + 10f

                    addText("Комментарий", photo.comment)
                    y += 10f
                }
            }

            document.finishPage(page)

            val fileName = "Гидроотчёт_${SimpleDateFormat(
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

        startActivity(Intent.createChooser(intent, "Отправить PDF"))
    }
}
