package com.numsolve

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform