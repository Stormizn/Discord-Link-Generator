plugins {
    id("java")
    id("application")
}

group = "me.stormizn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.3.2")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

application {
    mainClass.set("me.stormizn.bot.DiscordBot")
}

tasks.test {
    useJUnitPlatform()
}