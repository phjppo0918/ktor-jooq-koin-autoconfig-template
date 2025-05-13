package com.example

import io.ktor.server.application.Application
import io.ktor.server.tomcat.jakarta.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
}
