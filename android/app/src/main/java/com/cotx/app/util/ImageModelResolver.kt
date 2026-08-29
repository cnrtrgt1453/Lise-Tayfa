package com.cotx.app.util

import android.util.Base64

object ImageModelResolver {
    /**
     * Resolves image model for Coil AsyncImage.
     * Handles Base64 Data URIs (data:image/...) by decoding to ByteArray so Coil can render it natively.
     */
    fun resolve(url: String?): Any? {
        if (url.isNullOrEmpty()) return null
        if (url.startsWith("data:image/")) {
            return try {
                val base64Data = url.substringAfter("base64,")
                Base64.decode(base64Data, Base64.DEFAULT)
            } catch (e: Exception) {
                url
            }
        }
        return url
    }
}
