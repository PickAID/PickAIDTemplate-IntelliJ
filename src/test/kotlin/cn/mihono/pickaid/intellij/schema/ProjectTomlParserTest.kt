package cn.mihono.pickaid.intellij.schema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectTomlParserTest {
    @Test
    fun `parses tables and assignments`() {
        val parsed = ProjectTomlParser.parse(
            """
            schema_version = 1

            [mod]
            mod_id = "example"
            authors = ["A", "B"]
            """.trimIndent(),
        )

        assertEquals(listOf("mod"), parsed.tables.map { it.name })
        assertEquals("schema_version", parsed.assignments[0].key)
        assertEquals(null, parsed.assignments[0].tableName)
        assertEquals("mod", parsed.assignments[1].tableName)
        assertEquals(listOf("A", "B"), ProjectTomlParser.stringArrayValues(parsed.assignments[2].value))
    }

    @Test
    fun `detects table completion context`() {
        val text = """
            schema_version = 1

            [mod]
            mod_id = "example"

            [
        """.trimIndent()

        val context = ProjectTomlParser.completionContext(text, text.length)

        assertEquals(CompletionMode.Table, context.mode)
    }

    @Test
    fun `detects key completion context inside table`() {
        val text = """
            [languages]
            kot
        """.trimIndent()

        val context = ProjectTomlParser.completionContext(text, text.length)

        assertEquals(CompletionMode.Key, context.mode)
        assertEquals("languages", context.tableName)
    }

    @Test
    fun `detects value completion context`() {
        val text = """
            [features]
            jei =
        """.trimIndent()

        val context = ProjectTomlParser.completionContext(text, text.length)

        assertEquals(CompletionMode.Value, context.mode)
        assertEquals("features", context.tableName)
        assertEquals("jei", context.key)
    }

    @Test
    fun `keeps comments inside quoted strings`() {
        assertEquals("description = \"a # b\"", ProjectTomlParser.stripComment("description = \"a # b\" # real").trim())
    }

    @Test
    fun `schema knows dynamic tables`() {
        assertTrue(PickAidTemplateSchema.isKnownTable("dependencies.deobf_compile_only"))
        assertTrue(PickAidTemplateSchema.isKnownTable("native_libraries.physics"))
        assertTrue(PickAidTemplateSchema.isKnownTable("mod_relations.optional"))
    }

    @Test
    fun `schema knows refined publish tables`() {
        assertTrue(PickAidTemplateSchema.isKnownTable("publish.mods"))
        assertTrue(PickAidTemplateSchema.isKnownTable("publish.modrinth"))
        assertTrue(PickAidTemplateSchema.isKnownTable("publish.curseforge"))
        assertEquals(ValueKind.String, PickAidTemplateSchema.keySpec("publish.mods", "changelog_file")?.kind)
        assertEquals(ValueKind.Enum, PickAidTemplateSchema.keySpec("publish.mods", "changelog_section")?.kind)
        assertEquals(ValueKind.Boolean, PickAidTemplateSchema.keySpec("publish", "publish_maven_before_upload")?.kind)
        assertEquals(ValueKind.StringArray, PickAidTemplateSchema.keySpec("publish.curseforge", "game_versions")?.kind)
    }
}
