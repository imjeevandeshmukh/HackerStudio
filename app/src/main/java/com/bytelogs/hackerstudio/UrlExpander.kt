package com.bytelogs.hackerstudio

import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import android.os.StrictMode


object UrlExpander {

    @Throws(IOException::class)
     fun expandUrl(shortenedUrl: String): String {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val url = URL(shortenedUrl)
        val httpURLConnection = url.openConnection(Proxy.NO_PROXY) as HttpURLConnection
        httpURLConnection.instanceFollowRedirects = false
        val expandedURL = httpURLConnection.getHeaderField("Location")
        httpURLConnection.disconnect()
        return expandedURL
    }
}