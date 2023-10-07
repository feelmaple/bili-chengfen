plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.15.0"
}

group = "com.feelmaple.mirai.plugin"
version = "0.3.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

mirai {
    jvmTarget = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    "shadowLink"("com.google.code.gson:gson")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    "shadowLink"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("io.ktor:ktor-client-core:1.6.4")
    "shadowLink"("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-json:1.6.4")
    "shadowLink"("io.ktor:ktor-client-json")
    implementation("io.ktor:ktor-client-serialization:1.6.4")
    "shadowLink"("io.ktor:ktor-client-serialization")
    implementation("io.ktor:ktor-client-cio:1.6.4")
    "shadowLink"("io.ktor:ktor-client-cio")
    implementation("io.pebbletemplates:pebble:3.2.1")
    "shadowLink"("io.pebbletemplates:pebble")
    implementation("org.seleniumhq.selenium:selenium-java:4.8.1")
    compileOnly("net.mamoe.yamlkt:yamlkt:0.12.0")
}