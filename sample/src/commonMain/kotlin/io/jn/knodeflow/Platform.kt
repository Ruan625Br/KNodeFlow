package io.jn.knodeflow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform