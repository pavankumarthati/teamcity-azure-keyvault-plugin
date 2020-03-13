buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }

  dependencies {
    classpath("com.github.rodm:gradle-teamcity-plugin:1.2.2")
  }
}

plugins {
  kotlin("jvm") version "1.3.70" apply false
}

group = "com.github.vyadh.teamcity"
version = "1.1.0"

extra["teamcityVersion"] = findProperty("teamcity.version") ?: "2018.1.2"

subprojects {
  tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }
}
