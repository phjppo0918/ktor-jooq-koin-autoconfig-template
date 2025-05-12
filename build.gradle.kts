
import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.meta.kotlin.database
import org.jooq.meta.kotlin.forcedType
import org.jooq.meta.kotlin.forcedTypes
import org.jooq.meta.kotlin.generate
import org.jooq.meta.kotlin.generator
import org.jooq.meta.kotlin.jdbc
import org.jooq.meta.kotlin.schema
import org.jooq.meta.kotlin.schemata
import org.jooq.meta.kotlin.target

val koinVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val jooqVersion: String by project
val mysqlVersion: String by project
val hikariCPVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("nu.studer.jooq") version "10.1"
    id("org.flywaydb.flyway") version "11.8.1"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.tomcat.jakarta.EngineMain"
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}
sourceSets {
    main {
        kotlin {
            srcDirs("src/main/kotlin", "src/generated")
        }
    }
}

configurations {
    create("flywayMigration")
}

dependencies {
    // ktor
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-request-validation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-tomcat-jakarta")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-config-yaml")

    // koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // db
    implementation("com.mysql:mysql-connector-j:$mysqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    add("flywayMigration", "com.mysql:mysql-connector-j:$mysqlVersion")

    // jooq
    implementation("org.jooq:jooq:$jooqVersion")
    jooqGenerator(project(":jooq-configuration"))
    jooqGenerator("org.jooq:jooq:$jooqVersion")

    // etc
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host")
}

// https://github.com/etiennestuder/gradle-jooq-plugin/blob/main/example/configure_jooq_with_flyway/build.gradle

val dbUrl = project.findProperty("db.url").toString()
val dbUser = project.findProperty("db.user").toString()
val dbPassword = project.findProperty("db.password").toString()
val dbSchema = project.findProperty("db.schema").toString()

flyway {
    configurations = arrayOf("flywayMigration")
    url = dbUrl
    user = dbUser
    password = dbPassword
    driver = "com.mysql.cj.jdbc.Driver"
}

jooq {
    version.set(jooqVersion)
    configurations {
        create("main") {
            jooqConfiguration {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc {
                    driver = flyway.driver
                    url = flyway.url
                    user = flyway.user
                    password = flyway.password
                }
                generator {
                    name = "org.jooq.meta.mysql.MySQLDatabase"
                    strategy.name = "jooq.configuration.generator.JPrefixGeneratorStrategy"
                    database {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        isUnsignedTypes = true
                        schemata {
                            schema {
                                inputSchema = dbSchema
                            }
                        }
                        forcedTypes {
                            forcedType {
                                includeTypes = "int unsigned"
                                userType = "java.lang.Long"
                            }
                            forcedType {
                                includeTypes = "tinyint unsigned"
                                userType = "java.lang.Integer"
                            }
                            forcedType {
                                includeTypes = "smallint unsigned"
                                userType = "java.lang.Integer"
                            }
                            forcedType {
                                includeTypes = "TINYINT\\(1\\)"
                                userType = "java.lang.Boolean"
                            }
                        }
                    }
                    generate {
                        isDaos = true
                        isRecords = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                        isDeprecated = false
                    }

                    target {
                        directory = "src/generated"
                    }
                }
            }
        }
    }
}

tasks.named<JooqGenerate>("generateJooq").configure {
    dependsOn(tasks.named("flywayMigrate"))

    inputs
        .files(fileTree("src/main/resources/db/migration"))
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    allInputsDeclared.set(true)
}
