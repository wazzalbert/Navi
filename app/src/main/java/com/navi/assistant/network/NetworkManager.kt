package com.navi.assistant.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Pemeriksaan konektivitas. Pada Tahap 5 kelas ini akan diperluas untuk klien
 * HTTP ke penyedia LLM; saat ini hanya menjawab "apakah internet tersedia?".
 */
object NetworkManager {

    fun isOnline(context: Context): Boolean {
        val manager = context.applicationContext
            .getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
