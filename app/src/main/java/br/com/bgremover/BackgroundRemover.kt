package br.com.bgremover

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Remove o fundo de uma imagem usando ML Kit Subject Segmentation.
 * Processamento 100% no dispositivo (sem internet).
 */
object BackgroundRemover {

    private val segmenter by lazy {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundBitmap()
            .build()
        SubjectSegmentation.getClient(options)
    }

    /**
     * Retorna um Bitmap ARGB com fundo transparente.
     */
    suspend fun removeBackground(source: Bitmap): Bitmap {
        val image = InputImage.fromBitmap(source, 0)

        val result = suspendCancellableCoroutine { cont ->
            segmenter.process(image)
                .addOnSuccessListener { segmentationResult ->
                    cont.resume(segmentationResult)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

        // Preferência: bitmap já recortado pelo ML Kit
        val foreground = result.foregroundBitmap
        if (foreground != null) {
            return ensureArgb(foreground)
        }

        // Fallback: aplica a máscara de confiança manualmente
        val mask = result.foregroundConfidenceMask
            ?: throw IllegalStateException("ML Kit não retornou máscara nem bitmap de foreground.")

        return applyMask(source, mask)
    }

    private fun applyMask(source: Bitmap, mask: java.nio.FloatBuffer): Bitmap {
        val w = source.width
        val h = source.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        mask.rewind()
        for (i in pixels.indices) {
            val confidence = if (mask.hasRemaining()) mask.get() else 0f
            if (confidence < 0.5f) {
                pixels[i] = Color.TRANSPARENT
            }
        }

        out.setPixels(pixels, 0, w, 0, 0, w, h)
        return out
    }

    private fun ensureArgb(bitmap: Bitmap): Bitmap {
        if (bitmap.config == Bitmap.Config.ARGB_8888) return bitmap
        val copy = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(copy)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return copy
    }

    fun close() {
        try {
            segmenter.close()
        } catch (_: Exception) {
        }
    }
}
