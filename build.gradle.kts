import org.apache.ibatis.jdbc.ScriptRunner
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileReader
import java.sql.DriverManager
import kotlin.use

plugins {
    id("java")
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.3.0"
    id("nu.studer.jooq") version "9.0"
}

buildscript {
    dependencies {
        classpath("org.mybatis:mybatis:3.5.13")
        classpath("org.xerial:sqlite-jdbc:3.44.1.0")
    }
}

group = "dsh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jooq:jooq:3.19.6")

    // JDBC drivers
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.postgresql:postgresql:42.7.3")

    // Jooq generators
    jooqGenerator("org.slf4j:slf4j-simple:2.0.12")
    jooqGenerator("org.xerial:sqlite-jdbc:3.44.1.0")

    intellijPlatform {
        create("IC", "2024.2.5")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}

jooq {
    version.set("3.19.6")
    configurations {
        create("sqlite") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.sqlite.JDBC"
                    url = "jdbc:sqlite:todo.sqlite"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
//                        inputSchema = "main"
                    }
                    target.apply {
                        packageName = "dsh.todoplusplugin.jooq"
                        directory = "$buildDir/generated-src/jooq/sqlite"
                    }
                }
            }
        }
    }
}

val runSqlScript by tasks.registering(RunSqlScriptTask::class) {
    dbUrl.set("jdbc:sqlite:$projectDir/todo.sqlite")
    dbUser.set("user")
    dbPassword.set("password")
}

tasks.named("generateSqliteJooq") {
    dependsOn(runSqlScript)
}

tasks.named("compileKotlin") {
    dependsOn("generateSqliteJooq")
}

@CacheableTask
abstract class RunSqlScriptTask : DefaultTask() {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    val sqlScript: File = project.file("src/main/resources/init.sql")

    @Input
    val dbUrl = project.objects.property(String::class.java).convention("jdbc:sqlite:todo.sqlite")

    @Input
    val dbUser = project.objects.property(String::class.java).convention("")

    @Input
    val dbPassword = project.objects.property(String::class.java).convention("")

    @TaskAction
    fun runScript() {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection(dbUrl.get(), dbUser.get(), dbPassword.get()).use { connection ->
            connection.autoCommit = false
            val runner = ScriptRunner(connection)
            runner.setSendFullScript(true)
            FileReader(sqlScript).use { reader ->
                runner.runScript(reader)
            }
            connection.commit()
        }
    }
}
