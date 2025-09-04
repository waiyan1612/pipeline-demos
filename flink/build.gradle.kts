plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.flink.streaming)
    implementation(libs.flink.table.bridge)
    runtimeOnly(libs.flink.table.planner)
    implementation(libs.flink.clients)
    implementation(libs.flink.json)
    implementation(libs.flink.connector.base)
    implementation(libs.flink.kinesis)
}
