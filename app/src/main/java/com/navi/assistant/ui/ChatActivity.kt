package com.navi.assistant.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.navi.assistant.R

/**
 * Layar percakapan. Pada Tahap 1 hanya kerangka yang bisa dibuka dari widget;
 * input teks dan riwayat dibuat di Tahap 2.
 */
class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        findViewById<android.view.View>(R.id.chat_root).applySystemBarPadding()
        findViewById<Button>(R.id.chat_back_button).setOnClickListener { finish() }
    }
}
