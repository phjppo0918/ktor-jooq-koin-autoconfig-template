val jooqVersion: String by project
val mysqlVersion: String by project

plugins {
    kotlin("jvm")
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq-codegen:$jooqVersion")
    implementation("com.mysql:mysql-connector-j:$mysqlVersion")
}
