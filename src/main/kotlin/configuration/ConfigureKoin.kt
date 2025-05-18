package com.example.configuration

import com.example.configuration.db.JooqConfiguration
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(JooqConfiguration().module, defaultModule)
    }
}
