package com.example.configuration.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class JooqConfiguration {
    @Single
    fun datasource(): HikariDataSource {
        val config =
            HikariConfig().apply {
                jdbcUrl = System.getenv("DB_URL") ?: "jdbc:mysql://localhost:3306/localdb"
                username = System.getenv("DB_USER") ?: "root"
                password = System.getenv("DB_PASSWORD") ?: "1234"
                driverClassName = "com.mysql.cj.jdbc.Driver"
            }
        return HikariDataSource(config)
    }

    @Single
    fun dslContext(): DSLContext = DSL.using(datasource(), SQLDialect.MYSQL)
}
