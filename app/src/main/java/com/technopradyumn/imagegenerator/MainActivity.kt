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
            Toast.makeText(this@MainActivity, "Waiting...", Toast.LENGTH_LONG).show()
            if (editText.text.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter prompt...", Toast.LENGTH_LONG)
                    .show()
            } else {

                if (imageView.drawable == null) {
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
                                if (images.isNotEmpty()) {
                                    val imageUrl = images[0].url
                                    loadAndDisplayImage(imageUrl, imageView)
                                    currentImageUrl = imageUrl


                                } else {
                                    showErrorMessage("No images found.")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showErrorMessage("An error occurred: ${e.message}")
                        }
                    }

                }else{
                    imageView.setImageDrawable(null)
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
                                if (images.isNotEmpty()) {
                                    val imageUrl = images[0].url
                                    loadAndDisplayImage(imageUrl, imageView)
                                    currentImageUrl = imageUrl


                                } else {
                                    showErrorMessage("No images found.")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showErrorMessage("An error occurred: ${e.message}")
                        }
                    }
                }
            }
        }

        downloadBtn.setOnClickListener {
            currentImageUrl?.let { it1 -> saveImageToExternalStorage(it1) }
        }

        shareBtn.setOnClickListener {
            currentImageUrl?.let {  shareImage() }

        }


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

        Toast.makeText(this@MainActivity, "Image Downloading...", Toast.LENGTH_LONG).show()

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
                    Toast.makeText(this@MainActivity, "Image saved to Downloads folder.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorMessage("Failed to save the image to Downloads folder.")
            }
        }
    }

    private fun shareImage() {
        val intent= Intent(Intent.ACTION_SEND)
        intent.type="text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Hey, Checkout this cool meme $currentImageUrl")
        val chooser = Intent.createChooser(intent, "Share this meme")
        startActivity(chooser)
    }

}
