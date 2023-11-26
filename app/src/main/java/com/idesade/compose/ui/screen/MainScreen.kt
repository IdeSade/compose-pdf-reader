package com.idesade.compose.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.idesade.compose.pdf.PdfReader
import com.idesade.compose.ui.theme.AppTheme
import java.io.File

data class PdfFile(
    val name: String,
    val file: File? = null,
)

@Composable
fun MainScreen(
    data: List<PdfFile>
) {
    var currentPdfFile by remember { mutableStateOf(data.firstOrNull()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(1f),
        ) {
            PdfReader(
                file = currentPdfFile?.file
            )
        }

        data.forEach { pdfFile ->
            Button(
                modifier = Modifier.fillMaxWidth(0.6f),
                onClick = {
                    currentPdfFile = pdfFile
                }
            ) {
                Text(text = pdfFile.name)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview
@Composable
fun PreviewMainScreen() {
    AppTheme {
        MainScreen(
            data = listOf(
                PdfFile("Pdf from images"),
                PdfFile("Vector pdf")
            )
        )
    }
}