package com.wutsi.codegen.util

object CaseUtil {
    fun toCamelCase(str: String, capitalizeFirstLetter: Boolean): String {
        val buff = StringBuilder()
        var part = false
        for (i in 0..str.length - 1) {
            val ch = str[i]
            if (buff.isEmpty()) {
                buff.append(if (capitalizeFirstLetter) ch.toUpperCase() else ch.toLowerCase())
            } else if (ch.isLetterOrDigit()) {
                if (part) {
                    buff.append(ch.toUpperCase())
                    part = false
                } else {
                    buff.append(ch)
                }
            } else {
                part = true
            }
        }
        return buff.toString()
    }

    fun toSnakeCase(str: String): String {
        val buff = StringBuilder()
        for (i in 0..str.length - 1) {
            val ch = str[i]
            if (!ch.isLetterOrDigit()) {
                buff.append("-")
            } else {
                buff.append(ch)
            }
        }
        return buff.toString()
    }
}
