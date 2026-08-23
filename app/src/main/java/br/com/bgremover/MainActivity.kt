package br.com.bgremover

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF1E88E5),
                    secondary = Color(0xFF42A5F5),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BgRemoverScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BackgroundRemover.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BgRemoverScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Selecione uma imagem para começar") }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bmp = loadBitmap(context, uri)
            originalBitmap = bmp
            resultBitmap = null
            status = "Imagem carregada. Toque em Remover Fundo."
        } catch (e: Exception) {
            status = "Erro ao abrir imagem: ${e.message}"
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BG Remover",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Galeria")
                }

                Button(
                    onClick = {
                        val src = originalBitmap ?: return@Button
                        scope.launch {
                            isProcessing = true
                            status = "Processando com IA... Aguarde"
                            try {
                                val result = withContext(Dispatchers.Default) {
                                    BackgroundRemover.removeBackground(src)
                                }
                                resultBitmap = result
                                status = "Fundo removido com sucesso!"
                            } catch (e: Exception) {
                                status = "Erro: ${e.message}"
                                Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                            } finally {
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = originalBitmap != null && !isProcessing
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Remover")
                }

                Button(
                    onClick = {
                        val bmp = resultBitmap ?: return@Button
                        val ok = savePngToGallery(context, bmp)
                        status = if (ok) {
                            "Salvo na galeria!"
                        } else {
                            "Falha ao salvar"
                        }
                        Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = resultBitmap != null && !isProcessing
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Salvar")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isProcessing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = status,
                fontSize = 14.sp,
                color = Color(0xFFBDBDBD),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Antes / Depois
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviewCard(
                    title = "Antes",
                    bitmap = originalBitmap,
                    modifier = Modifier.weight(1f),
                    checkerboard = false
                )
                PreviewCard(
                    title = "Depois",
                    bitmap = resultBitmap,
                    modifier = Modifier.weight(1f),
                    checkerboard = true
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Processamento 100% no aparelho\n(Google ML Kit Subject Segmentation)",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PreviewCard(
    title: String,
    bitmap: Bitmap?,
    modifier: Modifier = Modifier,
    checkerboard: Boolean
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, Color(0xFF424242), RoundedCornerShape(12.dp))
                .background(
                    if (checkerboard) Color(0xFF2A2A2A) else Color(0xFF1A1A1A),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            } else {
                Text(
                    text = if (title == "Antes") "Sem imagem" else "Resultado",
                    color = Color(0xFF616161),
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun loadBitmap(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }.copy(Bitmap.Config.ARGB_8888, true)
    } else {
        @Suppress("DEPRECATION")
        android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            .copy(Bitmap.Config.ARGB_8888, true)
    }
}

private fun savePngToGallery(context: android.content.Context, bitmap: Bitmap): Boolean {
    return try {
        val filename = "bg_remover_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BG Remover")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false

        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
