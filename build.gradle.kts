plugins {
    `maven-publish`
    `java-platform`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

javaPlatform.allowDependencies()

val platformAttribute = Attribute.of("org.jetbrains.kotlin.platform.type", String::class.java)
val categoryAttribute = Attribute.of("org.gradle.category", String::class.java)

val bom by configurations.creating

val dep by configurations.creating {
    attributes {
        attribute(platformAttribute, "jvm")
        attribute(categoryAttribute, "library")
    }
}

dependencies {
    // external BOMs
    bom("org.jetbrains.kotlin:kotlin-bom:_")
    bom("org.jetbrains.kotlinx:kotlinx-coroutines-bom:_")
    bom("org.jetbrains.kotlinx:kotlinx-serialization-bom:_")
    bom("io.ktor:ktor-bom:_")
    bom("io.r2dbc:r2dbc-bom:_")
    bom("org.testcontainers:testcontainers-bom:_")
    bom("io.kotest:kotest-bom:_")
    bom("dev.whyoleg.cryptography:cryptography-bom:_")

    // Kotlin plugins
    dep("org.jetbrains.kotlinx:atomicfu-gradle-plugin:_")

    // Gradle plugins
    dep("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
    dep("org.jetbrains.dokka:dokka-gradle-plugin:_")
    dep("org.jetbrains.kotlinx:kover-gradle-plugin:_")
    dep("org.jmailen.gradle:kotlinter-gradle:_")
    dep("org.gradlex:reproducible-builds:_")
    dep("info.solidsoft.gradle.pitest:gradle-pitest-plugin:_")

    // build tools
    dep("com.pinterest.ktlint:ktlint-cli:_")

    // KSP
    dep("com.google.devtools.ksp:symbol-processing-api:_")
    dep("com.google.devtools.ksp:symbol-processing-gradle-plugin:_")
    dep("com.google.devtools.ksp:symbol-processing:_")
    dep("com.google.devtools.ksp:symbol-processing-cmdline:_")

    // misc Kotlinx libraries
    dep("org.jetbrains.kotlinx:kotlinx-collections-immutable:_")
    dep("org.jetbrains.kotlinx:kotlinx-datetime:_")
    dep("org.jetbrains.kotlinx:kotlinx-cli:_")
    dep("org.jetbrains.kotlinx:kotlinx-io-core:_")
    dep("org.jetbrains.kotlinx:kotlinx-io-bytestring:_")
    dep("org.jetbrains.kotlinx:atomicfu:_")
    dep("org.jetbrains.kotlinx:lincheck:_")

    // logging
    dep("ch.qos.logback:logback-classic:_")
    dep("org.slf4j:slf4j-api:_")
    dep("org.slf4j:slf4j-nop:_")
    dep("org.slf4j:slf4j-simple:_")

    // Kotlin Poet
    dep("com.squareup:kotlinpoet:_")
    dep("com.squareup:kotlinpoet-ksp:_")
    dep("com.squareup:kotlinpoet-metadata:_")

    // Mockk
    dep("io.mockk:mockk:_")
    dep("io.mockk:mockk-agent:_")

    // Mockative
    dep("io.mockative:mockative:_")
    dep("io.mockative:mockative-processor:_")

    // JUnit 5
    dep("org.junit.jupiter:junit-jupiter:_")
    dep("org.junit.jupiter:junit-jupiter-api:_")
    dep("org.junit.jupiter:junit-jupiter-engine:_")

    // AutoService
    dep("com.google.auto.service:auto-service-annotations:_")
    dep("com.google.auto.service:auto-service:_")

    // misc libraries
    dep("com.charleskorn.kaml:kaml:_")
    dep("io.github.pdvrieze.xmlutil:core:_")
    dep("io.github.pdvrieze.xmlutil:serialization:_")
    dep("com.ionspin.kotlin:bignum:_")
    dep("org.projectlombok:lombok:_")

    // Kotest extensions
    dep("io.kotest.extensions:kotest-extensions-pitest:_")

    bom.resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
        api(platform(dependency.module.toString()))
    }

    constraints {
        dep.resolvedConfiguration.firstLevelModuleDependencies.onEach { dependency ->
            api(dependency.module.toString())
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
    repositories {
        maven {
            name = "Local"
            url = uri("file://${rootProject.layout.buildDirectory.dir("repo").get().asFile.path}")
        }
        maven {
            name = "GitHub"
            url = uri("https://maven.pkg.github.com/handtruth/external-dependencies")
            credentials(PasswordCredentials::class) {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        val urlPrefix = System.getenv("CI_API_V4_URL")
        if (urlPrefix != null) {
            maven {
                name = "GitLab"
                val projectId = System.getenv("CI_PROJECT_ID")
                url = uri("$urlPrefix/projects/$projectId/packages/maven")
                credentials(HttpHeaderCredentials::class) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
}
