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
import androidx.activity.compose.setContent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        var installerName by remember { mutableStateOf("") }
        var installerCertificate by remember { mutableStateOf("") }
        var installerContact by remember { mutableStateOf("") }
        var siteAddress by remember { mutableStateOf("") }
        var clientRepresentative by remember { mutableStateOf("") }
        var workPeriod by remember { mutableStateOf("") }
        var substrateMaterial by remember { mutableStateOf("") }
        var instructionsRead by remember { mutableStateOf("") }
        var levelingCompound by remember { mutableStateOf("") }
        var primer by remember { mutableStateOf("") }
        var waterproofingMaterial by remember { mutableStateOf("") }
        var drainType by remember { mutableStateOf("") }
        var reinforcementTapes by remember { mutableStateOf("") }
        var penetrationSealants by remember { mutableStateOf("") }
        var adhesiveMortar by remember { mutableStateOf("") }
        var tiles by remember { mutableStateOf("") }
        var grout by remember { mutableStateOf("") }
        var elasticJoints by remember { mutableStateOf("") }
        var underfloorHeating by remember { mutableStateOf("") }
        var thresholdRise by remember { mutableStateOf("") }
        var floorSlope by remember { mutableStateOf("") }
        var wallArea by remember { mutableStateOf("") }
        var floorArea by remember { mutableStateOf("") }
        var wallConsumption by remember { mutableStateOf("") }
        var floorConsumption by remember { mutableStateOf("") }
        var wallThicknessRequirement by remember { mutableStateOf("") }
        var wallThicknessActual by remember { mutableStateOf("") }
        var wallThicknessResult by remember { mutableStateOf("") }
        var floorThicknessRequirement by remember { mutableStateOf("") }
        var floorThicknessActual by remember { mutableStateOf("") }
        var floorThicknessResult by remember { mutableStateOf("") }
        var result by remember { mutableStateOf("") }
        var supervisorName by remember { mutableStateOf("") }
        var supervisorCertificate by remember { mutableStateOf("") }
        var supervisorContact by remember { mutableStateOf("") }
        var reportDate by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("") }
        var showFieldDialog by remember { mutableStateOf(false) }
        var newFieldName by remember { mutableStateOf("") }
        var newFieldValue by remember { mutableStateOf("") }

        val scope = rememberCoroutineScope()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        if (showFieldDialog) {
            AlertDialog(
                onDismissRequest = { showFieldDialog = false },
                title = {
                    Text(stringResource(R.string.add_custom_field))
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newFieldName,
                            onValueChange = { newFieldName = it },
                            label = {
                                Text(stringResource(R.string.custom_field_name))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newFieldValue,
                            onValueChange = { newFieldValue = it },
                            label = {
                                Text(stringResource(R.string.custom_field_value))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFieldName.isNotBlank()) {
                                customFields.add(
                                    CustomField(
                                        mutableStateOf(newFieldName),
                                        mutableStateOf(newFieldValue)
                                    )
                                )
                            }

                            newFieldName = ""
                            newFieldValue = ""
                            showFieldDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFieldDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    }
                )
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
                    Text(
                        stringResource(R.string.section_installer),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = installerName,
                        onValueChange = { installerName = it },
                        label = { Text(stringResource(R.string.installer_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = installerCertificate,
                        onValueChange = { installerCertificate = it },
                        label = { Text(stringResource(R.string.installer_certificate)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = installerContact,
                        onValueChange = { installerContact = it },
                        label = { Text(stringResource(R.string.installer_contact)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        stringResource(R.string.section_site),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = siteAddress,
                        onValueChange = { siteAddress = it },
                        label = { Text(stringResource(R.string.site_address)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = clientRepresentative,
                        onValueChange = { clientRepresentative = it },
                        label = { Text(stringResource(R.string.client_representative)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = workPeriod,
                        onValueChange = { workPeriod = it },
                        label = { Text(stringResource(R.string.work_period)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = substrateMaterial,
                        onValueChange = { substrateMaterial = it },
                        label = { Text(stringResource(R.string.substrate_material)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.instructions_read))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = instructionsRead == stringResource(R.string.yes),
                            onClick = {
                                instructionsRead = stringResource(R.string.yes)
                            },
                            label = { Text(stringResource(R.string.yes)) }
                        )

                        FilterChip(
                            selected = instructionsRead == stringResource(R.string.no),
                            onClick = {
                                instructionsRead = stringResource(R.string.no)
                            },
                            label = { Text(stringResource(R.string.no)) }
                        )
                    }
                }

                item {
                    Text(
                        stringResource(R.string.section_system),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = levelingCompound,
                        onValueChange = { levelingCompound = it },
                        label = { Text(stringResource(R.string.leveling_compound)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = primer,
                        onValueChange = { primer = it },
                        label = { Text(stringResource(R.string.primer)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = waterproofingMaterial,
                        onValueChange = { waterproofingMaterial = it },
                        label = { Text(stringResource(R.string.waterproofing_material)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = drainType,
                        onValueChange = { drainType = it },
                        label = { Text(stringResource(R.string.drain_type_flange)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reinforcementTapes,
                        onValueChange = { reinforcementTapes = it },
                        label = { Text(stringResource(R.string.reinforcement_tapes)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = penetrationSealants,
                        onValueChange = { penetrationSealants = it },
                        label = { Text(stringResource(R.string.penetration_sealants)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = adhesiveMortar,
                        onValueChange = { adhesiveMortar = it },
                        label = { Text(stringResource(R.string.adhesive_mortar)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tiles,
                        onValueChange = { tiles = it },
                        label = { Text(stringResource(R.string.tiles)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = grout,
                        onValueChange = { grout = it },
                        label = { Text(stringResource(R.string.grout)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = elasticJoints,
                        onValueChange = { elasticJoints = it },
                        label = { Text(stringResource(R.string.elastic_joints)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = underfloorHeating,
                        onValueChange = { underfloorHeating = it },
                        label = { Text(stringResource(R.string.underfloor_heating)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = thresholdRise,
                        onValueChange = { thresholdRise = it },
                        label = { Text(stringResource(R.string.threshold_drain_rise)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = floorSlope,
                        onValueChange = { floorSlope = it },
                        label = { Text(stringResource(R.string.floor_slope)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        stringResource(R.string.section_quantities),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = wallArea,
                        onValueChange = { wallArea = it },
                        label = { Text(stringResource(R.string.wall_area)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = floorArea,
                        onValueChange = { floorArea = it },
                        label = { Text(stringResource(R.string.floor_area)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = wallConsumption,
                        onValueChange = { wallConsumption = it },
                        label = { Text(stringResource(R.string.wall_consumption)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = floorConsumption,
                        onValueChange = { floorConsumption = it },
                        label = { Text(stringResource(R.string.floor_consumption)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        stringResource(R.string.section_checks),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = wallThicknessRequirement,
                        onValueChange = { wallThicknessRequirement = it },
                        label = {
                            Text(stringResource(R.string.wall_thickness_requirement))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = wallThicknessActual,
                        onValueChange = { wallThicknessActual = it },
                        label = {
                            Text(stringResource(R.string.wall_thickness_actual))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.wall_thickness_result))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = wallThicknessResult ==
                                stringResource(R.string.meets_requirements),
                            onClick = {
                                wallThicknessResult =
                                    stringResource(R.string.meets_requirements)
                            },
                            label = {
                                Text(stringResource(R.string.meets_requirements))
                            }
                        )

                        FilterChip(
                            selected = wallThicknessResult ==
                                stringResource(R.string.does_not_meet_requirements),
                            onClick = {
                                wallThicknessResult =
                                    stringResource(R.string.does_not_meet_requirements)
                            },
                            label = {
                                Text(
                                    stringResource(R.string.does_not_meet_requirements)
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = floorThicknessRequirement,
                        onValueChange = { floorThicknessRequirement = it },
                        label = {
                            Text(stringResource(R.string.floor_thickness_requirement))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = floorThicknessActual,
                        onValueChange = { floorThicknessActual = it },
                        label = {
                            Text(stringResource(R.string.floor_thickness_actual))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.floor_thickness_result))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = floorThicknessResult ==
                                stringResource(R.string.meets_requirements),
                            onClick = {
                                floorThicknessResult =
                                    stringResource(R.string.meets_requirements)
                            },
                            label = {
                                Text(stringResource(R.string.meets_requirements))
                            }
                        )

                        FilterChip(
                            selected = floorThicknessResult ==
                                stringResource(R.string.does_not_meet_requirements),
                            onClick = {
                                floorThicknessResult =
                                    stringResource(R.string.does_not_meet_requirements)
                            },
                            label = {
                                Text(
                                    stringResource(R.string.does_not_meet_requirements)
                                )
                            }
                        )
                    }
                }

                item {
                    Text(
                        stringResource(R.string.inspection_result),
                        fontSize = 18.sp
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = result ==
                                stringResource(R.string.result_approved),
                            onClick = {
                                result = stringResource(R.string.result_approved)
                            },
                            label = {
                                Text(stringResource(R.string.result_approved))
                            }
                        )

                        FilterChip(
                            selected = result ==
                                stringResource(R.string.result_rejected),
                            onClick = {
                                result = stringResource(R.string.result_rejected)
                            },
                            label = {
                                Text(stringResource(R.string.result_rejected))
                            }
                        )
                    }
                }

                item {
                    Text(
                        stringResource(R.string.section_supervisor),
                        fontSize = 18.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = supervisorName,
                        onValueChange = { supervisorName = it },
                        label = { Text(stringResource(R.string.supervisor_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supervisorCertificate,
                        onValueChange = { supervisorCertificate = it },
                        label = {
                            Text(stringResource(R.string.supervisor_certificate))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = supervisorContact,
                        onValueChange = { supervisorContact = it },
                        label = {
                            Text(stringResource(R.string.supervisor_contact))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reportDate,
                        onValueChange = { reportDate = it },
                        label = { Text(stringResource(R.string.report_date)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        stringResource(R.string.custom_fields),
                        fontSize = 18.sp
                    )
                }

                items(customFields) { field ->
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(field.name.value)
                            Text(field.value.value)

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
                        onClick = { showFieldDialog = true },
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
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val bitmap = remember(photo.uri) {
                                loadBitmap(photo.uri, 900)
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = stringResource(
                                        R.string.pdf_photo
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            OutlinedTextField(
                                value = photo.comment.value,
                                onValueChange = { photo.comment.value = it },
                                label = {
                                    Text(stringResource(R.string.photo_comment))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            scope.launch {
                                statusMessage = getString(R.string.creating_pdf)

                                val uri = createPdf(
                                    installerName,
                                    installerCertificate,
                                    installerContact,
                                    siteAddress,
                                    clientRepresentative,
                                    workPeriod,
                                    substrateMaterial,
                                    instructionsRead,
                                    levelingCompound,
                                    primer,
                                    waterproofingMaterial,
                                    drainType,
                                    reinforcementTapes,
                                    penetrationSealants,
                                    adhesiveMortar,
                                    tiles,
                                    grout,
                                    elasticJoints,
                                    underfloorHeating,
                                    thresholdRise,
                                    floorSlope,
                                    wallArea,
                                    floorArea,
                                    wallConsumption,
                                    floorConsumption,
                                    wallThicknessRequirement,
                                    wallThicknessActual,
                                    wallThicknessResult,
                                    floorThicknessRequirement,
                                    floorThicknessActual,
                                    floorThicknessResult,
                                    result,
                                    supervisorName,
                                    supervisorCertificate,
                                    supervisorContact,
                                    reportDate.ifBlank {
                                        dateFormat.format(Date())
                                    }
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

                    if (statusMessage.isNotEmpty()) {
                        Text(statusMessage)
                    }
                }
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
        installerName: String,
        installerCertificate: String,
        installerContact: String,
        siteAddress: String,
        clientRepresentative: String,
        workPeriod: String,
        substrateMaterial: String,
        instructionsRead: String,
        levelingCompound: String,
        primer: String,
        waterproofingMaterial: String,
        drainType: String,
        reinforcementTapes: String,
        penetrationSealants: String,
        adhesiveMortar: String,
        tiles: String,
        grout: String,
        elasticJoints: String,
        underfloorHeating: String,
        thresholdRise: String,
        floorSlope: String,
        wallArea: String,
        floorArea: String,
        wallConsumption: String,
        floorConsumption: String,
        wallThicknessRequirement: String,
        wallThicknessActual: String,
        wallThicknessResult: String,
        floorThicknessRequirement: String,
        floorThicknessActual: String,
        floorThicknessResult: String,
        result: String,
        supervisorName: String,
        supervisorCertificate: String,
        supervisorContact: String,
        reportDate: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1

            var page = document.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            var canvas = page.canvas
            var y = 50f

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isFakeBoldText = true
            }

            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 9f
            }

            val commentPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
            }

            val textPaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 10f
            }

            fun nextPage() {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        pageNumber
                    ).create()
                )
                canvas = page.canvas
                y = 50f
            }

            fun addText(label: String, value: String) {
                val text = "$label: ${value.ifBlank { "—" }}"
                val layout = StaticLayout.Builder
                    .obtain(
                        text,
                        0,
                        text.length,
                        textPaint,
                        pageWidth - 100
                    )
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f, 1f)
                    .build()

                if (y + layout.height > pageHeight - 60) {
                    nextPage()
                }

                canvas.drawText(label, 50f, y, labelPaint)
                y += 12f

                canvas.save()
                canvas.translate(50f, y)
                layout.draw(canvas)
                canvas.restore()

                y += layout.height + 6f
            }

            fun addSection(titleText: String) {
                if (y > pageHeight - 100) {
                    nextPage()
                }

                y += 10f
                canvas.drawText(titleText, 50f, y, headerPaint)
                y += 18f
            }

            logoUri?.let { uri ->
                loadBitmap(uri, 400)?.let { logo ->
                    val scale = minOf(
                        120f / logo.width,
                        60f / logo.height
                    )

                    val logoWidth = logo.width * scale
                    val logoHeight = logo.height * scale

                    canvas.drawBitmap(
                        Bitmap.createScaledBitmap(
                            logo,
                            logoWidth.toInt().coerceAtLeast(1),
                            logoHeight.toInt().coerceAtLeast(1),
                            true
                        ),
                        pageWidth - 50f - logoWidth,
                        35f,
                        null
                    )
                }
            }

            canvas.drawText(
                getString(R.string.pdf_title),
                50f,
                y,
                titlePaint
            )
            y += 28f

            addSection(getString(R.string.pdf_installer_section))
            addText(getString(R.string.installer_name), installerName)
            addText(getString(R.string.installer_certificate), installerCertificate)
            addText(getString(R.string.installer_contact), installerContact)

            addSection(getString(R.string.pdf_site_section))
            addText(getString(R.string.site_address), siteAddress)
            addText(getString(R.string.client_representative), clientRepresentative)
            addText(getString(R.string.work_period), workPeriod)
            addText(getString(R.string.substrate_material), substrateMaterial)
            addText(getString(R.string.instructions_read), instructionsRead)

            addSection(getString(R.string.pdf_system_section))
            addText(getString(R.string.leveling_compound), levelingCompound)
            addText(getString(R.string.primer), primer)
            addText(getString(R.string.waterproofing_material), waterproofingMaterial)
            addText(getString(R.string.drain_type_flange), drainType)
            addText(getString(R.string.reinforcement_tapes), reinforcementTapes)
            addText(getString(R.string.penetration_sealants), penetrationSealants)
            addText(getString(R.string.adhesive_mortar), adhesiveMortar)
            addText(getString(R.string.tiles), tiles)
            addText(getString(R.string.grout), grout)
            addText(getString(R.string.elastic_joints), elasticJoints)
            addText(getString(R.string.underfloor_heating), underfloorHeating)
            addText(getString(R.string.threshold_drain_rise), thresholdRise)
            addText(getString(R.string.floor_slope), floorSlope)

            addSection(getString(R.string.pdf_quantities_section))
            addText(getString(R.string.wall_area), wallArea)
            addText(getString(R.string.floor_area), floorArea)
            addText(getString(R.string.wall_consumption), wallConsumption)
            addText(getString(R.string.floor_consumption), floorConsumption)

            addSection(getString(R.string.pdf_checks_section))
            addText(
                getString(R.string.wall_thickness_requirement),
                wallThicknessRequirement
            )
            addText(
                getString(R.string.wall_thickness_actual),
                wallThicknessActual
            )
            addText(
                getString(R.string.wall_thickness_result),
                wallThicknessResult
            )
            addText(
                getString(R.string.floor_thickness_requirement),
                floorThicknessRequirement
            )
            addText(
                getString(R.string.floor_thickness_actual),
                floorThicknessActual
            )
            addText(
                getString(R.string.floor_thickness_result),
                floorThicknessResult
            )

            if (customFields.isNotEmpty()) {
                addSection(getString(R.string.pdf_custom_fields))

                customFields.forEach { field ->
                    addText(field.name.value, field.value.value)
                }
            }

            addSection(getString(R.string.pdf_result_section))
            canvas.drawText(
                "${getString(R.string.pdf_result_label)} - " +
                    result.ifBlank { "—" },
                50f,
                y,
                titlePaint
            )
            y += 25f

            addSection(getString(R.string.pdf_supervisor_section))
            addText(getString(R.string.supervisor_name), supervisorName)
            addText(getString(R.string.supervisor_certificate), supervisorCertificate)
            addText(getString(R.string.supervisor_contact), supervisorContact)
            addText(getString(R.string.report_date), reportDate)

            photos.chunked(4).forEach { photoChunk ->
                nextPage()

                canvas.drawText(
                    getString(R.string.pdf_photos),
                    50f,
                    y,
                    headerPaint
                )
                y += 22f

                val margin = 50f
                val gap = 15f
                val cellWidth = (pageWidth - margin * 2 - gap) / 2f
                val cellHeight = 220f
                val rowHeight = cellHeight + 45f

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

                        canvas.drawBitmap(
                            Bitmap.createScaledBitmap(
                                bitmap,
                                drawWidth.toInt().coerceAtLeast(1),
                                drawHeight.toInt().coerceAtLeast(1),
                                true
                            ),
                            x + (cellWidth - drawWidth) / 2f,
                            imageTop + 12f + (cellHeight - drawHeight) / 2f,
                            null
                        )
                    }

                    canvas.drawText(
                        photo.comment.value.ifBlank { "—" }.take(80),
                        x,
                        imageTop + cellHeight + 22f,
                        commentPaint
                    )
                }
            }

            document.finishPage(page)

            val fileName = "Valvontapoytakirja_${SimpleDateFormat(
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

        startActivity(
            Intent.createChooser(
                intent,
                getString(R.string.share_pdf)
            )
        )
    }
}
