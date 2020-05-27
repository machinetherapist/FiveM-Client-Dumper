package com.gitlab.marcodsl.utils

fun sleepWhile(condition: Boolean) {
    while (condition)
        Thread.sleep(1)
}
