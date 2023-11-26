package com.technopradyumn.imagegenerator

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.client.OpenAI
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var currentImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val generateImageButton: ImageView = findViewById(R.id.generate_image_button)
        val imageView: ImageView = findViewById(R.id.generated_image_view)
        val editText: EditText = findViewById(R.id.textInputEditText)

        val downloadBtn: ImageView = findViewById(R.id.downloadBtn)
        val shareBtn: ImageView = findViewById(R.id.shareBtn)

        // Actual OpenAI API key
        val apiKey = "Actual OpenAI API key"
        val openAI = OpenAI(apiKey)

        generateImageButton.setOnClickListener {
            if (editText.text.isEmpty()) {
                showToast("Please enter a prompt...")
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val images = openAI.imageURL(
                            creation = ImageCreation(
                                prompt = editText.text.toString(),
                                n = 2,
                                size = ImageSize.is1024x1024
                            )
                        )
                        withContext(Dispatchers.Main) {
                            handleImageResponse(images, imageView)
                        }
                    } catch (e: Exception) {
                        handleImageError(e)
                    }
                }
            }
        }

        downloadBtn.setOnClickListener {
            currentImageUrl?.let { url ->
                saveImageToExternalStorage(url)
            }
        }

        shareBtn.setOnClickListener {
            currentImageUrl?.let {
                shareImage(it)
            }
        }
    }

    private fun handleImageResponse(images: List<ImageURL>, imageView: ImageView) {
        if (images.isNotEmpty()) {
            val imageUrl = images[0].url
            loadAndDisplayImage(imageUrl, imageView)
            currentImageUrl = imageUrl
        } else {
            showErrorMessage("No images found.")
        }
    }

    private fun handleImageError(e: Exception) {
        e.printStackTrace()
        showErrorMessage("An error occurred: ${e.message}")
    }

    private fun loadAndDisplayImage(url: String, imageView: ImageView) {
        Picasso.get()
            .load(url)
            .into(imageView)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun saveImageToExternalStorage(url: String) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "image_$timeStamp.png"

        showToast("Image Downloading...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = URL(url).openStream()
                val directory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, fileName)

                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
                outputStream.close()
                inputStream.close()

                withContext(Dispatchers.Main) {
                    showToast("Image saved to Downloads folder.")
                }
            } catch (e: Exception) {
                handleImageError(e)
            }
        }
    }

    private fun shareImage(url: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout this cool meme $url")
        val chooser = Intent.createChooser(intent, "Share this meme")
        startActivity(chooser)
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}
