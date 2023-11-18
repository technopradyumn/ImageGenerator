package com.technopradyumn.imagegenerator

import android.app.Application
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class MyApplication : Application() {

    // Define your OpenAI API key
    private val apiKey = "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" // Replace with your actual API key

    @OptIn(ExperimentalTime::class)
    fun initOpenAI(): OpenAI {
        // Create an instance of the OpenAI client
        return OpenAI(
            token = apiKey,
            timeout = Timeout(socket = 60.seconds),
            // additional configurations...
        )
    }
}
