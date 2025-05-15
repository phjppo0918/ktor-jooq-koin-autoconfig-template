package com.example

import com.example.configuration.configureKoin
import com.example.configuration.configureRouting
import io.ktor.server.application.Application
import io.ktor.server.tomcat.jakarta.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureKoin()
    configureRouting()
}
