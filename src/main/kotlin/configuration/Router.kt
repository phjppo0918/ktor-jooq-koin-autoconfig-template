package com.example.configuration

import io.ktor.server.routing.Route

interface Router {
    fun route(r: Route)
}
