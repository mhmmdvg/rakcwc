package com.rakcwc.utils

object GetInitial {
    fun usersInitial(name: String): String {
        if (name.isEmpty()) return ""

        val splitName = name.split(" ")
        return if (splitName.size > 1) {
            "${splitName[0][0]}${splitName[1][0]}"
        } else {
            "${splitName[0][0]}"
        }
    }
}