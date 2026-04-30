package cn.mihono.pickaid.intellij.completion

import cn.mihono.pickaid.intellij.schema.CompletionMode
import cn.mihono.pickaid.intellij.schema.PickAidTemplateProject
import cn.mihono.pickaid.intellij.schema.PickAidTemplateSchema
import cn.mihono.pickaid.intellij.schema.ProjectTomlParser
import cn.mihono.pickaid.intellij.schema.ValueKind
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.Document
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

class PickAidProjectTomlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet,
                ) {
                    val file = parameters.originalFile
                    if (!PickAidTemplateProject.isProjectToml(file)) {
                        return
                    }

                    val document = parameters.editor.document
                    val tomlContext = ProjectTomlParser.completionContext(document.text, parameters.offset)

                    when (tomlContext.mode) {
                        CompletionMode.Table -> addTableCompletions(result, tomlContext.lineStartOffset)
                        CompletionMode.Key -> addKeyCompletions(result, tomlContext.tableName, tomlContext.lineStartOffset)
                        CompletionMode.Value -> addValueCompletions(result, tomlContext.tableName, tomlContext.key, tomlContext.valueStartOffset)
                        CompletionMode.None -> Unit
                    }
                }
            },
        )
    }

    private fun addTableCompletions(result: CompletionResultSet, lineStartOffset: Int) {
        PickAidTemplateSchema.tableCompletions.forEach { tableName ->
            result.addElement(
                LookupElementBuilder
                    .create(tableName)
                    .withPresentableText("[$tableName]")
                    .withTypeText("PickAID table", true)
                    .withInsertHandler(replaceLineWith("[$tableName]", lineStartOffset)),
            )
        }
    }

    private fun addKeyCompletions(result: CompletionResultSet, tableName: String?, lineStartOffset: Int) {
        val tableSpec = PickAidTemplateSchema.tableSpec(tableName) ?: return

        tableSpec.keys.forEach { key ->
            result.addElement(
                LookupElementBuilder
                    .create(key.name)
                    .withTypeText(key.description, true)
                    .withInsertHandler(replaceLineWith("${key.name} = ${key.defaultInsertText}", lineStartOffset)),
            )
        }

        tableSpec.keyValueExample?.let { example ->
            val key = example.substringBefore('=').trim()
            result.addElement(
                LookupElementBuilder
                    .create(key)
                    .withPresentableText(example)
                    .withTypeText(tableSpec.description, true)
                    .withInsertHandler(replaceLineWith(example, lineStartOffset)),
            )
        }
    }

    private fun addValueCompletions(
        result: CompletionResultSet,
        tableName: String?,
        key: String?,
        valueStartOffset: Int?,
    ) {
        if (key == null || valueStartOffset == null) {
            return
        }

        val keySpec = PickAidTemplateSchema.keySpec(tableName, key) ?: return

        when (keySpec.kind) {
            ValueKind.Boolean -> addValue(result, "true", valueStartOffset)
                .also { addValue(result, "false", valueStartOffset) }

            ValueKind.Enum -> keySpec.enumValues.forEach { addValue(result, "\"$it\"", valueStartOffset) }
            ValueKind.StringArray -> {
                if (tableName?.startsWith("native_libraries.") == true && key == "platforms") {
                    PickAidTemplateSchema.nativePlatforms.forEach { platform ->
                        addValue(result, "[\"$platform\"]", valueStartOffset)
                    }
                } else {
                    addValue(result, keySpec.defaultInsertText, valueStartOffset)
                }
            }

            else -> addValue(result, keySpec.defaultInsertText, valueStartOffset)
        }
    }

    private fun addValue(
        result: CompletionResultSet,
        value: String,
        valueStartOffset: Int,
    ): LookupElement {
        val element = LookupElementBuilder
            .create(value)
            .withInsertHandler(replaceValueWith(value, valueStartOffset))
        result.addElement(element)
        return element
    }

    private fun replaceLineWith(text: String, lineStartOffset: Int): InsertHandler<LookupElement> =
        InsertHandler { context, _ ->
            val document = context.document
            val lineEndOffset = currentLineEnd(document, lineStartOffset)
            replaceRange(document, lineStartOffset, lineEndOffset, text)
            context.editor.caretModel.moveToOffset(lineStartOffset + text.length)
        }

    private fun replaceValueWith(text: String, valueStartOffset: Int): InsertHandler<LookupElement> =
        InsertHandler { context, _ ->
            val document = context.document
            val lineEndOffset = currentLineEnd(document, valueStartOffset)
            val start = skipWhitespace(document, valueStartOffset, lineEndOffset)
            replaceRange(document, start, lineEndOffset, text)
            context.editor.caretModel.moveToOffset(start + text.length)
        }

    private fun replaceRange(document: Document, startOffset: Int, endOffset: Int, text: String) {
        val start = startOffset.coerceIn(0, document.textLength)
        val end = endOffset.coerceIn(start, document.textLength)
        document.replaceString(start, end, text)
    }

    private fun skipWhitespace(document: Document, startOffset: Int, endOffset: Int): Int {
        var offset = startOffset.coerceAtMost(document.textLength)
        val end = endOffset.coerceAtMost(document.textLength)
        while (offset < end && document.charsSequence[offset].isWhitespace()) {
            offset += 1
        }
        return offset
    }

    private fun currentLineEnd(document: Document, offset: Int): Int {
        val lineNumber = document.getLineNumber(offset.coerceIn(0, document.textLength))
        return document.getLineEndOffset(lineNumber)
    }
}
