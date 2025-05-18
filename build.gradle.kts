import org.jooq.meta.kotlin.database
import org.jooq.meta.kotlin.forcedType
import org.jooq.meta.kotlin.forcedTypes
import org.jooq.meta.kotlin.generate
import org.jooq.meta.kotlin.generator
import org.jooq.meta.kotlin.jdbc
import org.jooq.meta.kotlin.schema
import org.jooq.meta.kotlin.schemata
import org.jooq.meta.kotlin.target
import java.util.Properties

val koinVersion: String by project
val koinAnnotationVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val jooqVersion: String by project
val mysqlVersion: String by project
val hikariCPVersion: String by project
val kotestVersion: String by project
val mockkVersion: String by project

plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
    id("nu.studer.jooq") version "10.1"
    id("org.flywaydb.flyway") version "11.8.1"
    id("com.google.devtools.ksp") version "2.1.20-1.0.32"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.tomcat.jakarta.EngineMain"
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }

    // TODO ktor 4.1.0 정식 출시 후 제거
    maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap") {
        mavenContent {
            includeGroupAndSubgroups("io.ktor")
        }
    }
}
kotlin {
    sourceSets.main {
        kotlin.srcDirs("src/main/kotlin", "src/generated", "build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDirs("src/main/kotlin", "src/generated", "build/generated/ksp/test/kotlin")
    }
}

configurations {
    create("flywayMigration")
}

buildscript {
    dependencies {
        classpath("com.mysql:mysql-connector-j:9.3.0")
        classpath("org.flywaydb:flyway-mysql:11.8.1")
    }
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
    implementation("io.insert-koin:koin-annotations:$koinAnnotationVersion")
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // db
    implementation("com.mysql:mysql-connector-j:$mysqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCPVersion")

    // jooq
    implementation("org.jooq:jooq:$jooqVersion")
    jooqGenerator(project(":jooq-configuration"))
    jooqGenerator("org.jooq:jooq:$jooqVersion")

    // etc
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // test
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

val envFile = file(".env")
if (envFile.exists()) {
    val props = Properties()
    envFile.reader().use { props.load(it) }
    props.forEach { key, value ->
        project.extensions.extraProperties[key.toString()] = value
    }
}

val dbUrl = project.findProperty("DB_URL").toString()
val dbUser = project.findProperty("DB_USER").toString()
val dbPassword = project.findProperty("DB_PASSWORD").toString()
val dbSchema = project.findProperty("DB_SCHEMA").toString()

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
                    name = "org.jooq.codegen.DefaultGenerator"
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

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}
