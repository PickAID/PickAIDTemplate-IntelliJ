package cn.mihono.pickaid.intellij.schema

data class TomlTable(
    val name: String,
    val lineNumber: Int,
    val startOffset: Int,
    val nameStartOffset: Int,
)

data class TomlAssignment(
    val tableName: String?,
    val key: String,
    val value: String,
    val lineNumber: Int,
    val startOffset: Int,
    val keyStartOffset: Int,
    val valueStartOffset: Int,
)

data class ParsedToml(
    val tables: List<TomlTable>,
    val assignments: List<TomlAssignment>,
)

data class TomlCompletionContext(
    val tableName: String?,
    val key: String?,
    val lineStartOffset: Int,
    val lineEndOffset: Int,
    val valueStartOffset: Int?,
    val mode: CompletionMode,
)

enum class CompletionMode {
    Table,
    Key,
    Value,
    None,
}

object ProjectTomlParser {
    private val tableRegex = Regex("""^\s*\[([A-Za-z0-9_.-]+)]\s*(?:#.*)?$""")
    private val assignmentRegex = Regex("""^\s*([A-Za-z0-9_.-]+)\s*=\s*(.*)$""")
    private val stringRegex = Regex(""""([^"\\]*(?:\\.[^"\\]*)*)"""")

    fun parse(text: String): ParsedToml {
        val tables = mutableListOf<TomlTable>()
        val assignments = mutableListOf<TomlAssignment>()
        var currentTable: String? = null
        var offset = 0

        text.lineSequence().forEachIndexed { lineNumber, rawLine ->
            val strippedLine = stripComment(rawLine)
            tableRegex.matchEntire(strippedLine)?.let { match ->
                val name = match.groupValues[1]
                val nameStart = offset + rawLine.indexOf(name).coerceAtLeast(0)
                tables += TomlTable(name, lineNumber, offset, nameStart)
                currentTable = name
            } ?: run {
                assignmentRegex.matchEntire(strippedLine)?.let { match ->
                    val key = match.groupValues[1]
                    val value = match.groupValues[2].trim()
                    val keyStart = offset + rawLine.indexOf(key).coerceAtLeast(0)
                    val valueStart = offset + rawLine.indexOf('=').coerceAtLeast(0) + 1
                    assignments += TomlAssignment(currentTable, key, value, lineNumber, offset, keyStart, valueStart)
                }
            }

            offset += rawLine.length + 1
        }

        return ParsedToml(tables, assignments)
    }

    fun completionContext(text: String, offset: Int): TomlCompletionContext {
        val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', offset).let { if (it == -1) text.length else it }
        val linePrefix = text.substring(lineStart, offset.coerceIn(lineStart, lineEnd))
        val lineFull = text.substring(lineStart, lineEnd)
        val trimmedPrefix = linePrefix.trimStart()
        val equalsIndex = lineFull.indexOf('=')
        val tableName = tableAtOffset(text, lineStart)

        if (trimmedPrefix.startsWith("[")) {
            return TomlCompletionContext(tableName, null, lineStart, lineEnd, null, CompletionMode.Table)
        }

        if (equalsIndex != -1 && lineStart + equalsIndex < offset) {
            val key = lineFull.substring(0, equalsIndex).trim()
            return TomlCompletionContext(tableName, key, lineStart, lineEnd, lineStart + equalsIndex + 1, CompletionMode.Value)
        }

        if (linePrefix.isBlank() || linePrefix.trim().matches(Regex("""[A-Za-z0-9_.-]*"""))) {
            return TomlCompletionContext(tableName, null, lineStart, lineEnd, null, CompletionMode.Key)
        }

        return TomlCompletionContext(tableName, null, lineStart, lineEnd, null, CompletionMode.None)
    }

    fun stringArrayValues(value: String): List<String> =
        stringRegex.findAll(value).map { it.groupValues[1] }.toList()

    fun isPickAidTomlName(name: String): Boolean = name == "project.toml" || name == "project.local.toml"

    fun isForge1201(text: String): Boolean =
        text.lineSequence().any { line ->
            line.trimStart().startsWith("template_version") && line.contains("1.20.1")
        }

    fun stripComment(line: String): String {
        var inString = false
        var escaped = false
        val builder = StringBuilder(line.length)

        for (char in line) {
            if (char == '"' && !escaped) {
                inString = !inString
            }

            if (char == '#' && !inString) {
                break
            }

            builder.append(char)
            escaped = char == '\\' && !escaped
            if (char != '\\') {
                escaped = false
            }
        }

        return builder.toString()
    }

    private fun tableAtOffset(text: String, lineStartOffset: Int): String? {
        val beforeLine = text.substring(0, lineStartOffset.coerceIn(0, text.length))
        var currentTable: String? = null

        beforeLine.lineSequence().forEach { line ->
            val stripped = stripComment(line)
            tableRegex.matchEntire(stripped)?.let { match ->
                currentTable = match.groupValues[1]
            }
        }

        return currentTable
    }
}
