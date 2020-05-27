import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "com.gitlab.marcodsl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // CLI stuff
    implementation("commons-cli:commons-cli:1.4")

    // Logging stuff
    implementation("org.slf4j:slf4j-api:1.7.5")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Gson
    implementation("com.google.code.gson:gson:2.8.6")

    // CDP Client
    implementation("pl.wendigo:chrome-reactive-kotlin:0.6.1")

    // Ktor
    val ktorVersion = "1.3.2"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
    resources.srcDir("src/main/resources")
}

tasks {

    withType<ShadowJar> {
        manifest {
            attributes["Main-Class"] = "com.gitlab.marcodsl.Main"
        }
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}