package com.gitlab.marcodsl.utils

import java.util.*

fun String.toPlainText(): String {
    val bytes = Base64.getMimeDecoder().decode(this)
    return String(bytes)
}