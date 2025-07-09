package org.jagrati.jagratibackend.util

object PidGenerator {
    fun generatePid(timeInMillis: Long = System.currentTimeMillis(), name: String): String{
        val sanitizedName = name.replace(Regex("[^a-zA-Z0-9]"), "_")
        return "${sanitizedName}_$timeInMillis"
    }
}