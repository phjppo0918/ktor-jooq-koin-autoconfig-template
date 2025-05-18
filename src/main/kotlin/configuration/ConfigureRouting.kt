package com.example.configuration

import com.example.member.router.memberRouter
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        memberRouter()
    }
}
