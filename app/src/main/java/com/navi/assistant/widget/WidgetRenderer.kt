package com.navi.assistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.navi.assistant.R
import com.navi.assistant.data.NaviState
import com.navi.assistant.network.NetworkManager
import com.navi.assistant.ui.ChatActivity
import java.util.Calendar

/**
 * Membangun RemoteViews widget. Hanya membaca data kecil (SharedPreferences dan
 * status jaringan), tidak ada proses AI di sini.
 */
object WidgetRenderer {

    const val ACTION_REFRESH = "com.navi.assistant.action.REFRESH_WIDGET"

    private const val REQUEST_OPEN_CHAT = 1
    private const val REQUEST_REFRESH = 2

    fun build(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.navi_widget)

        // Sapaan personal sesuai jam.
        val greetingRes = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..10 -> R.string.greeting_morning
            in 11..14 -> R.string.greeting_afternoon
            in 15..17 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
        val userName = NaviState.getUserName(context)
        views.setTextViewText(R.id.widget_greeting, context.getString(greetingRes, userName))

        // Status tautan.
        val online = NetworkManager.isOnline(context)
        views.setTextViewText(
            R.id.widget_link_status,
            context.getString(if (online) R.string.status_online else R.string.status_offline)
        )
        views.setTextColor(
            R.id.widget_link_status,
            ContextCompat.getColor(context, if (online) R.color.navi_green else R.color.navi_amber)
        )

        // Status AI Core.
        val core = NaviState.getCoreStatus(context)
        views.setTextViewText(R.id.widget_core_status, context.getString(R.string.widget_core_format, core.label))

        // Pesan terakhir.
        val last = NaviState.getLastMessage(context, context.getString(R.string.widget_default_message))
        views.setTextViewText(R.id.widget_message, context.getString(R.string.widget_message_format, last))

        // Tombol membuka percakapan.
        val chatIntent = Intent(context, ChatActivity::class.java)
        views.setOnClickPendingIntent(
            R.id.widget_open_button,
            PendingIntent.getActivity(
                context, REQUEST_OPEN_CHAT, chatIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        // Mengetuk bagian widget lain menyegarkan status (aman, tidak mengubah data).
        val refreshIntent = Intent(context, NaviWidget::class.java).setAction(ACTION_REFRESH)
        views.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getBroadcast(
                context, REQUEST_REFRESH, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )

        return views
    }

    /** Menyegarkan semua widget NAVI yang terpasang. */
    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, NaviWidget::class.java))
        if (ids.isEmpty()) return
        manager.updateAppWidget(ids, build(context))
    }
}
