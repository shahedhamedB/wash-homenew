package com.washathomes.base.extension

fun String.toCamelCase(): String {
    if (this.isEmpty()) {
        return ""
    } else {
//        return this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()
        val words = this.split(" ")
        if (words.size < 2) {
            return this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()
        } else {
            var formattedString = ""
            for (i in words.indices) {
                if (words[i].isEmpty()) {
                    formattedString += words[i]
                } else {
                    formattedString += words[i].substring(0, 1)
                        .toUpperCase() + words[i].substring(1)
                        .toLowerCase()
                }
                if (i < words.size - 1) {
                    formattedString += " "
                }
            }
            return formattedString
        }
    }
}