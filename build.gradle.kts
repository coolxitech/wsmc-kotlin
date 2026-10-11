import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("java-library")
    kotlin("jvm") version libs.versions.kotlinVersion
    kotlin("kapt") version libs.versions.kotlinVersion
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.2"
}

@Suppress("PropertyName")
val mod_version: String by project
@Suppress("PropertyName")
val maven_group: String by project
@Suppress("PropertyName")
val mod_id: String by project
@Suppress("PropertyName")
val archives_base_name: String by project
@Suppress("PropertyName")
val forgelin_continuous_version: String by project
@Suppress("PropertyName")
val use_mixins: String by project
@Suppress("PropertyName")
val use_coremod: String by project
@Suppress("PropertyName")
val coremod_plugin_class_name: String by project

group = maven_group
version = mod_version
base {
    archivesName.set(archives_base_name)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // 使用本地已安装的 Adoptium Temurin 8，避免额外下载 Azul JDK
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

configurations {
    val embed = create("embed")
    implementation.configure {
        extendsFrom(embed)
    }
}

minecraft {
    mcVersion.set("1.12.2")

    // MCP Mappings
    mcpMappingChannel.set("stable")
    mcpMappingVersion.set("39")

    // Set username here
    username.set("Developer")

    val args = mutableListOf("-ea:${group}")
    if (use_coremod.toBoolean()) {
        args += "-Dfml.coreMods.load=$coremod_plugin_class_name"
    }
    if (use_mixins.toBoolean()) {
        args += "-Dmixin.hotSwap=true"
        args += "-Dmixin.checks.interfaces=true"
        args += "-Dmixin.debug.export=true"
    }
    extraRunJvmArguments.addAll(args)

    useDependencyAccessTransformers.set(true)

    injectedTags.put("VERSION", project.version)
    injectedTags.put("MOD_ID", mod_id)
    injectedTags.put("MOD_NAME", archives_base_name)
}

tasks.injectTags.configure {
    outputClassName.set("${maven_group}.${archives_base_name}.Tags")
}

// RFG 2.0.2 在 Gradle 8.10+ 下不会为反编译/注入的源集声明隐式依赖，需显式补上
// kapt 会为每个源集生成 stub 任务（kaptGenerateStubs*Kotlin），同样需要补依赖
val rfgSourceSetDeps = mapOf(
    "PatchedMc" to "decompressDecompiledSources",
    "InjectedTags" to "injectTags",
    "McLauncher" to "createMcLauncherFiles"
)
rfgSourceSetDeps.forEach { (sourceSet, producer) ->
    tasks.matching {
        it.name.startsWith("compile${sourceSet}") || it.name.startsWith("kaptGenerateStubs${sourceSet}")
    }.configureEach {
        dependsOn(producer)
    }
}

repositories {
    maven {
        name = "CleanroomMC Maven"
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        name = "SpongePowered Maven"
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    mavenLocal()
}

dependencies {
    implementation("io.github.chaosunity.forgelin:Forgelin-Continuous:${forgelin_continuous_version}") {
        exclude("net.minecraftforge")
    }

    if (use_mixins.toBoolean()) {
        val mixin = "zone.rong:mixinbooter:11.0"
        implementation(mixin) {
            isTransitive = false
        }
        // mixin 注解处理器必须通过 kapt 运行，才能处理 Kotlin 里的 @Mixin 并生成 refmap
        kapt(mixin)
        // enableMixins 设置 refmap 名，并让 RFG 在检测到 kapt 插件后注入 -AreobfSrgFile/-AoutSrgFile/-AoutRefMapFile
        api(modUtils.enableMixins(mixin, "wsmc.refmap.json"))
    }
}

kapt {
    javacOptions {
        // Kotlin companion object 里的 @JvmStatic 会把 @Inject 同时复制到 Companion 实例方法，
        // Mixin AP 会把它误判为 "non-mixin method"。静态桥接方法仍会被正确处理，禁用该检查即可。
        option("-AMSG_INJECTOR_ON_NON_MIXIN_METHOD=disabled")
    }
}

// RFG 的 enableMixins 会用 withType(KaptTask) 把 mixins.srg / refmap 声明为【所有】kapt 任务的输出，
// 而 RFG 会为反编译/注入的源集（patchedMc/api/injectedTags/mcLauncher/injectedInterfaces）各建一个 kapt 任务。
// 这些源集没有 @Mixin，执行时会清空 main 源集 kaptKotlin 生成的 mixins.srg / refmap 且不再生成。
// 因此只保留 main 源集的 kaptKotlin，禁用其余所有 kapt 任务；reobfJar 显式依赖（disabled 的）kaptTestKotlin 以满足隐式依赖校验。
tasks.matching {
    it.name.startsWith("kapt") &&
        it.name.endsWith("Kotlin") &&
        !it.name.startsWith("kaptGenerateStubs") &&
        it.name != "kaptKotlin"
}.configureEach {
    enabled = false
}

tasks.named("reobfJar") {
    dependsOn("kaptTestKotlin")
}

tasks.withType<ProcessResources> {
    val mcVersion = minecraft.mcVersion.get()
    inputs.property("version", mod_version)
    inputs.property("mcversion", mcVersion)

    filesMatching(arrayListOf("mcmod.info", "pack.mcmeta")) {
        expand(
            "version" to mod_version,
            "mcversion" to mcVersion
        )
    }
}

tasks.withType<Jar> {
    manifest {
        val attributeMap = mutableMapOf<String, String>()
        if (use_coremod.toBoolean()) {
            attributeMap["FMLCorePlugin"] = coremod_plugin_class_name
        }
        if (use_mixins.toBoolean()) {
            attributeMap["MixinConfigs"] = "wsmc.mixins.json"
        }
        attributes(attributeMap)
    }
    from(provider {
        configurations.getByName("embed").map {
            if (it.isDirectory()) it else zipTree(it)
        }
    })
}

idea {
    module {
        inheritOutputDirs = true
    }
    project {
        settings {
            runConfigurations {
                add(Gradle("1. Run Client").apply {
                    setProperty("taskNames", listOf("runClient"))
                })
                add(Gradle("2. Run Server").apply {
                    setProperty("taskNames", listOf("runServer"))
                })
                add(Gradle("3. Run Obfuscated Client").apply {
                    setProperty("taskNames", listOf("runObfClient"))
                })
                add(Gradle("4. Run Obfuscated Server").apply {
                    setProperty("taskNames", listOf("runObfServer"))
                })
            }
            compiler.javac {
                afterEvaluate {
                    javacAdditionalOptions = "-encoding utf8"
                }
            }
        }
    }
}

tasks.named("processIdeaSettings").configure {
    dependsOn("injectTags")
}
