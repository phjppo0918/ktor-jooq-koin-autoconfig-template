package com.example

import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.tomcat.jakarta.EngineMain
        .main(args)
}

fun Application.module() {
    configureHTTP()
}
