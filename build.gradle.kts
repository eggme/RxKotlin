plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    testImplementation("io.mockk:mockk:1.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}