
package com.firewalltest

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.ViewGroup
import android.widget.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private var stopped = false
    private lateinit var address: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(20, 30, 45)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(15, 22, 35))
        }

        fun label(text: String) {
            layout.addView(TextView(this).apply {
                this.text = text
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(0, 12, 0, 12)
            })
        }

        label("Firewall Load Test")
        label("Проверка HTTPS-доступности сервера")

        address = EditText(this).apply {
            hint = "https://example.com"
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
        }
        layout.addView(address)

        val check = Button(this).apply {
            text = "Проверить сервер"
            setOnClickListener { checkServer() }
        }
        layout.addView(check)

        result = TextView(this).apply {
            text = "Готово к проверке"
            textSize = 16f
            setTextColor(Color.GREEN)
            setPadding(0, 24, 0, 0)
        }
        layout.addView(result)

        setContentView(ScrollView(this).apply {
            addView(layout)
        })
    }

    private fun checkServer() {
        val target = address.text.toString().trim()
        if (!target.startsWith("https://")) {
            result.text = "Введи корректный HTTPS-адрес."
            return
        }

        result.text = "Проверка..."
        Thread {
            val message = try {
                val start = System.currentTimeMillis()
                val conn = URL(target).openConnection()
                    as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val code = conn.responseCode
                val elapsed = System.currentTimeMillis() - start
                conn.disconnect()
                "HTTP: $code\nЗадержка: ${elapsed} мс"
            } catch (e: Exception) {
                "Ошибка: ${e.javaClass.simpleName}"
            }

            runOnUiThread { result.text = message }
        }.start()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
