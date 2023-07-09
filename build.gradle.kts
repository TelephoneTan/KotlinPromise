import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    val p = Properties().apply {
        val ins = FileInputStream("./local.gradle.properties")
        load(ins)
        ins.close()
    }
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/TelephoneTan/JavaPromise")
        credentials {
            username = p.getProperty("gpr_github_user") as String
            password = p.getProperty("gpr_github_key") as String
        }
    }
}

dependencies {
    implementation("pub.telephone:java-promise:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass.set("MainKt")
}