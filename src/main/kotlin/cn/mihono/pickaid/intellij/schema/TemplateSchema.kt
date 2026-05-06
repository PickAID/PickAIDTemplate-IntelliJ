package cn.mihono.pickaid.intellij.schema

enum class ValueKind {
    Boolean,
    Integer,
    String,
    StringArray,
    Url,
    MavenNotation,
    InlineTable,
    Enum,
}

data class KeySpec(
    val name: String,
    val kind: ValueKind,
    val description: String,
    val required: Boolean = false,
    val enumValues: List<String> = emptyList(),
    val defaultInsertText: String = defaultTextFor(kind, enumValues),
)

data class TableSpec(
    val name: String,
    val description: String,
    val keys: List<KeySpec> = emptyList(),
    val allowsArbitraryKeys: Boolean = false,
    val keyValueExample: String? = null,
)

object PickAidTemplateSchema {
    const val FORGE_1201_KUBEJS = "dev.latvian.mods:kubejs-forge:2001.6.5-build.16"

    val topLevelKeys = listOf(
        KeySpec("schema_version", ValueKind.Integer, "PickAID template configuration schema version."),
        KeySpec("template_version", ValueKind.String, "Template family and release version."),
    )

    private val modKeys = listOf(
        KeySpec("mod_id", ValueKind.String, "Forge mod id. Lowercase letters, digits, and underscores.", required = true, defaultInsertText = "\"example\""),
        KeySpec("mod_name", ValueKind.String, "Human-readable mod name.", required = true, defaultInsertText = "\"Example Mod\""),
        KeySpec("version", ValueKind.String, "Base mod version.", required = true, defaultInsertText = "\"1.0.0\""),
        KeySpec("version_suffix", ValueKind.String, "Optional suffix appended to the base version.", defaultInsertText = "\"\""),
        KeySpec("group", ValueKind.String, "Maven group and default Java package.", required = true, defaultInsertText = "\"com.example.examplemod\""),
        KeySpec("authors", ValueKind.StringArray, "Author list.", required = true, defaultInsertText = "[\"Your Name\"]"),
        KeySpec("license", ValueKind.String, "License identifier written to metadata.", required = true, defaultInsertText = "\"MIT\""),
        KeySpec("description", ValueKind.String, "Mod description written to metadata.", required = true, defaultInsertText = "\"What this mod does.\""),
        KeySpec("credits", ValueKind.String, "Optional credits text.", defaultInsertText = "\"\""),
        KeySpec("issue_tracker_url", ValueKind.Url, "Issue tracker URL.", defaultInsertText = "\"https://github.com/org/repo/issues\""),
    )

    private val featureKeys = booleanKeys(
        "jei" to "Just Enough Items API integration.",
        "curios" to "Curios API integration.",
        "geckolib" to "GeckoLib animation integration.",
        "player_animator" to "Player Animator integration.",
        "mixin_extras" to "MixinExtras support.",
    )

    private val languageKeys = booleanKeys(
        "kotlin" to "Enable Kotlin source support under src/main/kotlin.",
    )

    private val devPackKeys = booleanKeys(
        "basic" to "JEI + Jade local development helper pack.",
        "appleskin" to "AppleSkin local HUD helper pack.",
        "combat" to "Combat balancing helper pack.",
        "curios" to "Curios local runtime support.",
        "spell" to "Spell and attribute smoke-test helper pack.",
    )

    private val overrideKeys = listOf(
        "jei_version",
        "curios_version",
        "geckolib_version",
        "player_animator_version",
        "bendylib_version",
        "mixin_extras_version",
    ).map { KeySpec(it, ValueKind.String, "Override the template default for $it.", defaultInsertText = "\"\"") }

    private val metadataKeys = listOf(
        KeySpec("logo_file", ValueKind.String, "Metadata logo file.", defaultInsertText = "\"icon.png\""),
        KeySpec("logo_blur", ValueKind.Boolean, "Whether the metadata logo should blur.", defaultInsertText = "true"),
        KeySpec("show_as_resource_pack", ValueKind.Boolean, "Show this mod as a resource pack.", defaultInsertText = "false"),
        KeySpec("show_as_data_pack", ValueKind.Boolean, "Show this mod as a data pack.", defaultInsertText = "false"),
        KeySpec("update_json_url", ValueKind.Url, "Forge update JSON URL.", defaultInsertText = "\"\""),
        KeySpec("display_url", ValueKind.Url, "Project display URL.", defaultInsertText = "\"\""),
        KeySpec(
            "display_test",
            ValueKind.Enum,
            "Forge display test.",
            enumValues = listOf("MATCH_VERSION", "IGNORE_SERVER_VERSION", "IGNORE_ALL_VERSION", "NONE"),
            defaultInsertText = "\"MATCH_VERSION\"",
        ),
    )

    private val nativeKeys = listOf(
        KeySpec("load_name", ValueKind.String, "Library name passed to the native loader.", required = true, defaultInsertText = "\"pickaid_native\""),
        KeySpec(
            "loader",
            ValueKind.Enum,
            "Native load strategy.",
            required = true,
            enumValues = listOf("jni", "jna"),
            defaultInsertText = "\"jni\"",
        ),
        KeySpec(
            "platforms",
            ValueKind.StringArray,
            "Supported native binary platforms.",
            required = true,
            defaultInsertText = "[\"windows-x86_64\", \"linux-x86_64\", \"macos-aarch64\"]",
        ),
        KeySpec("required", ValueKind.Boolean, "Whether missing native binaries should fail the build.", defaultInsertText = "true"),
    )

    private val publishKeys = listOf(
        KeySpec("maven_url", ValueKind.Url, "Maven repository URL.", defaultInsertText = "\"\""),
        KeySpec("publish_maven_before_upload", ValueKind.Boolean, "Run Maven publishing before Modrinth or CurseForge upload.", defaultInsertText = "false"),
        KeySpec("maven_user", ValueKind.String, "Temporary Maven username fallback. Prefer project.local.toml or environment variables.", defaultInsertText = "\"\""),
        KeySpec("maven_password", ValueKind.String, "Temporary Maven password fallback. Prefer project.local.toml or environment variables.", defaultInsertText = "\"\""),
        KeySpec("modrinth_token", ValueKind.String, "Temporary Modrinth token fallback. Prefer project.local.toml or environment variables.", defaultInsertText = "\"\""),
        KeySpec("curseforge_token", ValueKind.String, "Temporary CurseForge token fallback. Prefer project.local.toml or environment variables.", defaultInsertText = "\"\""),
        KeySpec("curseforge_project", ValueKind.Integer, "CurseForge project id.", defaultInsertText = "0"),
        KeySpec("modrinth_project", ValueKind.String, "Modrinth project id.", defaultInsertText = "\"\""),
        KeySpec(
            "release_type",
            ValueKind.Enum,
            "Upload release channel.",
            enumValues = listOf("alpha", "beta", "release"),
            defaultInsertText = "\"alpha\"",
        ),
    )

    private val publishModsKeys = listOf(
        KeySpec(
            "release_type",
            ValueKind.Enum,
            "Shared upload release channel.",
            enumValues = listOf("alpha", "beta", "release"),
            defaultInsertText = "\"alpha\"",
        ),
        KeySpec("version_name", ValueKind.String, "Shared platform version name. Supports template tokens.", defaultInsertText = "\"[{mc_version}] {mod_name} {version}\""),
        KeySpec("display_name", ValueKind.String, "Shared CurseForge display name. Supports template tokens.", defaultInsertText = "\"[{mc_version}] {mod_name} - {version}\""),
        KeySpec("changelog", ValueKind.String, "Inline changelog for the uploaded version.", defaultInsertText = "\"\""),
        KeySpec("changelog_file", ValueKind.String, "Path to changelog text used for the uploaded version.", defaultInsertText = "\"CHANGELOG.md\""),
        KeySpec(
            "changelog_section",
            ValueKind.Enum,
            "How changelog_file is read. version extracts the current version section; full uploads the whole file.",
            enumValues = listOf("version", "full"),
            defaultInsertText = "\"version\"",
        ),
        KeySpec(
            "changelog_type",
            ValueKind.Enum,
            "Changelog markup type.",
            enumValues = listOf("text", "markdown", "html"),
            defaultInsertText = "\"markdown\"",
        ),
        KeySpec("game_versions", ValueKind.StringArray, "Minecraft versions for platform upload.", defaultInsertText = "[\"26.1.2\"]"),
        KeySpec("loaders", ValueKind.StringArray, "Mod loaders for platform upload.", defaultInsertText = "[\"neoforge\"]"),
    )

    private val publishModrinthKeys = listOf(
        KeySpec("project", ValueKind.String, "Modrinth project slug or id. Blank disables Modrinth upload.", defaultInsertText = "\"\""),
        KeySpec("token", ValueKind.String, "Local Modrinth token fallback. Prefer project.local.toml or MODRINTH_TOKEN.", defaultInsertText = "\"\""),
        KeySpec("version_name", ValueKind.String, "Modrinth version name override.", defaultInsertText = "\"[{mc_version}] {mod_name} {version}\""),
        KeySpec("changelog", ValueKind.String, "Modrinth inline changelog for this upload.", defaultInsertText = "\"\""),
        KeySpec("changelog_file", ValueKind.String, "Path to Modrinth changelog text.", defaultInsertText = "\"CHANGELOG.md\""),
        KeySpec(
            "changelog_section",
            ValueKind.Enum,
            "How changelog_file is read. version extracts the current version section; full uploads the whole file.",
            enumValues = listOf("version", "full"),
            defaultInsertText = "\"version\"",
        ),
        KeySpec("game_versions", ValueKind.StringArray, "Minecraft versions for Modrinth.", defaultInsertText = "[\"26.1.2\"]"),
        KeySpec("loaders", ValueKind.StringArray, "Loaders for Modrinth.", defaultInsertText = "[\"neoforge\"]"),
        KeySpec("sync_body_file", ValueKind.String, "Markdown file used to sync the Modrinth project body.", defaultInsertText = "\"README.MD\""),
        KeySpec("debug", ValueKind.Boolean, "Run Minotaur upload in debug mode without uploading.", defaultInsertText = "false"),
    )

    private val publishCurseforgeKeys = listOf(
        KeySpec("project", ValueKind.Integer, "CurseForge numeric project id. Zero disables CurseForge upload.", defaultInsertText = "0"),
        KeySpec("token", ValueKind.String, "Local CurseForge token fallback. Prefer project.local.toml or CURSEFORGE_TOKEN.", defaultInsertText = "\"\""),
        KeySpec("display_name", ValueKind.String, "CurseForge display name override.", defaultInsertText = "\"[{mc_version}] {mod_name} - {version}\""),
        KeySpec("changelog", ValueKind.String, "CurseForge inline changelog for this upload.", defaultInsertText = "\"\""),
        KeySpec("changelog_file", ValueKind.String, "Path to CurseForge changelog text.", defaultInsertText = "\"CHANGELOG.md\""),
        KeySpec(
            "changelog_section",
            ValueKind.Enum,
            "How changelog_file is read. version extracts the current version section; full uploads the whole file.",
            enumValues = listOf("version", "full"),
            defaultInsertText = "\"version\"",
        ),
        KeySpec(
            "changelog_type",
            ValueKind.Enum,
            "CurseForge changelog markup type.",
            enumValues = listOf("text", "markdown", "html"),
            defaultInsertText = "\"markdown\"",
        ),
        KeySpec("game_versions", ValueKind.StringArray, "CurseForge game version labels.", defaultInsertText = "[\"26.1.2\", \"NeoForge\"]"),
        KeySpec("manual_release", ValueKind.Boolean, "Mark CurseForge upload as manual release.", defaultInsertText = "false"),
        KeySpec("parent_file_id", ValueKind.Integer, "CurseForge parent file id for child files.", defaultInsertText = "0"),
    )

    private val namingKeys = listOf(
        KeySpec("archive_name", ValueKind.String, "Base archive name.", required = true, defaultInsertText = "\"example\""),
        KeySpec("jar_format", ValueKind.String, "Archive naming pattern.", required = true, defaultInsertText = "\"{archive_name}-{mc_version}-{version}\""),
    )

    private val runKeys = listOf(
        KeySpec("mc_user", ValueKind.InlineTable, "Local Minecraft user override.", defaultInsertText = "{ name = \"Dev\", uuid = \"00000000-0000-0000-0000-000000000000\" }"),
    )

    val dependencyBuckets = listOf(
        "api",
        "implementation",
        "compile_only",
        "compile_only_api",
        "runtime_only",
        "annotation_processor",
        "deobf_api",
        "deobf_compile_only",
        "deobf_compile_only_api",
        "deobf_implementation",
        "deobf_runtime_only",
        "jarjar",
    )

    val embeddedProjectBuckets = listOf("api", "implementation")
    val modRelationBuckets = listOf("required", "optional", "incompatible", "embedded")

    val nativePlatforms = listOf(
        "windows-x86_64",
        "windows-aarch64",
        "linux-x86_64",
        "linux-aarch64",
        "macos-x86_64",
        "macos-aarch64",
    )

    val staticTables = listOf(
        TableSpec("mod", "Mod identity.", modKeys),
        TableSpec("features", "Built-in ecosystem integration switches.", featureKeys),
        TableSpec("languages", "Language switches. Java is always enabled.", languageKeys),
        TableSpec("dev_packs", "Local-only development helper packs.", devPackKeys),
        TableSpec("overrides", "Curated dependency version overrides.", overrideKeys),
        TableSpec("repositories", "Additional Maven repositories.", allowsArbitraryKeys = true, keyValueExample = "custom_repo = \"https://repo.example.com/releases\""),
        TableSpec("dependencies", "Container for dependency buckets.", allowsArbitraryKeys = false),
        TableSpec("metadata", "Optional mod metadata.", metadataKeys),
        TableSpec("publish", "Maven publishing plus legacy flat platform upload keys.", publishKeys),
        TableSpec("publish.mods", "Shared Modrinth and CurseForge upload defaults.", publishModsKeys),
        TableSpec("publish.modrinth", "Modrinth upload configuration.", publishModrinthKeys),
        TableSpec("publish.curseforge", "CurseForge upload configuration.", publishCurseforgeKeys),
        TableSpec("naming", "Artifact naming.", namingKeys),
        TableSpec("run", "Local run configuration, usually in project.local.toml.", runKeys),
    )

    val tableCompletions: List<String> =
        staticTables.map { it.name } +
            dependencyBuckets.map { "dependencies.$it" } +
            embeddedProjectBuckets.map { "embedded_projects.$it" } +
            modRelationBuckets.map { "mod_relations.$it" } +
            listOf("native_libraries.physics")

    fun tableSpec(tableName: String?): TableSpec? {
        if (tableName == null || tableName.isBlank()) {
            return TableSpec("", "Top-level project fields.", topLevelKeys)
        }

        staticTables.firstOrNull { it.name == tableName }?.let { return it }

        return when {
            tableName.startsWith("dependencies.") && tableName.substringAfter("dependencies.") in dependencyBuckets ->
                TableSpec(
                    tableName,
                    "Dependency aliases for ${tableName.substringAfter("dependencies.")}.",
                    allowsArbitraryKeys = true,
                    keyValueExample = "example = \"com.example:library:1.0.0\"",
                )

            tableName.startsWith("native_libraries.") && tableName.substringAfter("native_libraries.").isNotBlank() ->
                TableSpec(tableName, "Prebuilt native library declaration.", nativeKeys)

            tableName.startsWith("embedded_projects.") && tableName.substringAfter("embedded_projects.") in embeddedProjectBuckets ->
                TableSpec(
                    tableName,
                    "Gradle subprojects embedded into the produced mod.",
                    allowsArbitraryKeys = true,
                    keyValueExample = "core = \":core\"",
                )

            tableName.startsWith("mod_relations.") && tableName.substringAfter("mod_relations.") in modRelationBuckets ->
                TableSpec(
                    tableName,
                    "Mod dependency, optional integration, incompatibility, or embedded metadata.",
                    allowsArbitraryKeys = true,
                    keyValueExample = "curios = \"[5.9.1,)\"",
                )

            else -> null
        }
    }

    fun isKnownTable(tableName: String): Boolean = tableSpec(tableName) != null

    fun keySpec(tableName: String?, key: String): KeySpec? = tableSpec(tableName)?.keys?.firstOrNull { it.name == key }

    fun missingRequiredModKeys(assignments: List<TomlAssignment>): List<String> {
        val present = assignments
            .filter { it.tableName == "mod" }
            .mapTo(mutableSetOf()) { it.key }

        return modKeys
            .filter { it.required && it.name !in present }
            .map { it.name }
    }

    private fun booleanKeys(vararg entries: Pair<String, String>): List<KeySpec> =
        entries.map { (key, description) ->
            KeySpec(key, ValueKind.Boolean, description, defaultInsertText = "false")
        }
}

private fun defaultTextFor(kind: ValueKind, enumValues: List<String>): String =
    when (kind) {
        ValueKind.Boolean -> "false"
        ValueKind.Integer -> "0"
        ValueKind.String -> "\"\""
        ValueKind.StringArray -> "[\"\"]"
        ValueKind.Url -> "\"https://example.com\""
        ValueKind.MavenNotation -> "\"com.example:library:1.0.0\""
        ValueKind.InlineTable -> "{ }"
        ValueKind.Enum -> enumValues.firstOrNull()?.let { "\"$it\"" } ?: "\"\""
    }
