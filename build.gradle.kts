plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    testImplementation("io.mockk:mockk:1.9.3")
}