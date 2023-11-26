package com.idesade.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.idesade.compose.ui.screen.MainScreen
import com.idesade.compose.ui.screen.PdfFile
import com.idesade.compose.ui.theme.AppTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainScreen(
                    data = listOf(
                        createPdfFileFromAssets("test.pdf", "Pdf from images"),
                        createPdfFileFromAssets("test2.pdf", "Vector pdf"),
                        createPdfFileFromAssets("lorem_ipsum.pdf", "Lorem ipsum pdf"),
                    )
                )
            }
        }
    }

    private fun createPdfFileFromAssets(fileName: String, name: String): PdfFile {
        val file = File.createTempFile("fromAssets", "deleteOnExit", cacheDir).apply { deleteOnExit() }
        assets.open(fileName).use { input ->
            FileOutputStream(file).use(input::copyTo)
        }

        return PdfFile(
            name = name,
            file = file,
        )
    }

}