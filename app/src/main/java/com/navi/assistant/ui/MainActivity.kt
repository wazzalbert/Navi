package com.navi.assistant.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.navi.assistant.R
import com.navi.assistant.data.NaviState
import com.navi.assistant.network.NetworkManager
import com.navi.assistant.widget.NaviWidget
import com.navi.assistant.widget.WidgetRenderer

/** Layar utama: animasi booting, pengaturan nama, dan pemasangan widget. */
class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var bootLog: TextView
    private lateinit var feedback: TextView
    private lateinit var nameInput: EditText

    private var bootLines: List<String> = emptyList()
    private val rendered = StringBuilder()
    private var lineIndex = 0
    private var charIndex = 0
    private var bootDone = false
    private var cursorOn = true

    private val typeRunnable = object : Runnable {
        override fun run() {
            if (lineIndex >= bootLines.size) {
                finishBoot()
                return
            }
            val line = bootLines[lineIndex]
            if (charIndex < line.length) {
                rendered.append(line[charIndex])
                charIndex++
                bootLog.text = rendered.toString() + CURSOR
                handler.postDelayed(this, CHAR_DELAY_MS)
            } else {
                rendered.append('\n')
                lineIndex++
                charIndex = 0
                bootLog.text = rendered.toString() + CURSOR
                handler.postDelayed(this, LINE_DELAY_MS)
            }
        }
    }

    private val blinkRunnable = object : Runnable {
        override fun run() {
            cursorOn = !cursorOn
            bootLog.text = rendered.toString() + if (cursorOn) CURSOR else " "
            handler.postDelayed(this, BLINK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main_root).applySystemBarPadding()

        bootLog = findViewById(R.id.boot_log)
        feedback = findViewById(R.id.feedback_text)
        nameInput = findViewById(R.id.name_input)

        if (NaviState.hasUserName(this)) {
            nameInput.setText(NaviState.getUserName(this))
        }

        findViewById<Button>(R.id.save_name_button).setOnClickListener { saveName() }
        findViewById<Button>(R.id.pin_widget_button).setOnClickListener { requestPinWidget() }
        findViewById<Button>(R.id.open_chat_button).setOnClickListener {
            startActivity(Intent(this, com.navi.assistant.ui.ChatActivity::class.java))
        }
        nameInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveName()
                true
            } else {
                false
            }
        }

        bootLines = buildBootLines()
        val skipAnimation = savedInstanceState != null || animationsDisabled()
        if (skipAnimation) {
            lineIndex = bootLines.size
            rendered.append(bootLines.joinToString("\n")).append('\n')
            bootLog.text = rendered.toString() + CURSOR
            bootDone = true
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(if (bootDone) blinkRunnable else typeRunnable)
    }

    override fun onResume() {
        super.onResume()
        WidgetRenderer.refreshAll(this)
    }

    override fun onStop() {
        handler.removeCallbacksAndMessages(null)
        super.onStop()
    }

    private fun finishBoot() {
        bootDone = true
        handler.post(blinkRunnable)
    }

    private fun buildBootLines(): List<String> {
        val link = getString(if (NetworkManager.isOnline(this)) R.string.status_word_online else R.string.status_word_offline)
        return listOf(
            getString(R.string.boot_line_header),
            getString(R.string.boot_line_memory),
            getString(R.string.boot_line_core),
            getString(R.string.boot_line_link, link),
            getString(R.string.boot_line_widget),
            getString(R.string.boot_line_ready)
        )
    }

    private fun animationsDisabled(): Boolean =
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    private fun saveName() {
        val saved = NaviState.setUserName(this, nameInput.text.toString())
        nameInput.setText(if (NaviState.hasUserName(this)) saved else "")
        nameInput.clearFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(nameInput.windowToken, 0)
        feedback.text = getString(R.string.feedback_name_saved, saved)
        WidgetRenderer.refreshAll(this)
    }

    private fun requestPinWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, NaviWidget::class.java)
        val requested = manager.isRequestPinAppWidgetSupported &&
            manager.requestPinAppWidget(provider, null, null)
        feedback.setText(if (requested) R.string.feedback_pin_requested else R.string.feedback_pin_manual)
    }

    private companion object {
        const val CURSOR = "_"
        const val CHAR_DELAY_MS = 18L
        const val LINE_DELAY_MS = 180L
        const val BLINK_MS = 530L
    }
}
