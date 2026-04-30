package cn.mihono.pickaid.intellij.inspection

import cn.mihono.pickaid.intellij.quickfix.CreateNativePlatformDirectoryFix
import cn.mihono.pickaid.intellij.quickfix.ReplaceKubeJsVersionFix
import cn.mihono.pickaid.intellij.schema.PickAidTemplateProject
import cn.mihono.pickaid.intellij.schema.PickAidTemplateSchema
import cn.mihono.pickaid.intellij.schema.ProjectTomlParser
import cn.mihono.pickaid.intellij.schema.TomlAssignment
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import java.io.File

class PickAidProjectTomlInspection : LocalInspectionTool() {
    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
    ): Array<ProblemDescriptor>? {
        if (!PickAidTemplateProject.isProjectToml(file)) {
            return null
        }

        val text = file.text
        val parsed = ProjectTomlParser.parse(text)
        val problems = mutableListOf<ProblemDescriptor>()

        parsed.tables.forEach { table ->
            if (!PickAidTemplateSchema.isKnownTable(table.name)) {
                problems += descriptor(
                    file,
                    manager,
                    table.nameStartOffset,
                    table.nameStartOffset + table.name.length,
                    "Unknown PickAID template table '${table.name}'.",
                    isOnTheFly,
                )
            }
        }

        parsed.assignments.forEach { assignment ->
            val tableSpec = PickAidTemplateSchema.tableSpec(assignment.tableName)
            if (tableSpec != null && !tableSpec.allowsArbitraryKeys && PickAidTemplateSchema.keySpec(assignment.tableName, assignment.key) == null) {
                problems += descriptor(
                    file,
                    manager,
                    assignment.keyStartOffset,
                    assignment.keyStartOffset + assignment.key.length,
                    "Unknown key '${assignment.key}' in ${assignment.tableName ?: "top-level"}.",
                    isOnTheFly,
                )
            }
        }

        if (file.name == "project.toml") {
            PickAidTemplateSchema.missingRequiredModKeys(parsed.assignments).forEach { missingKey ->
                val anchor = parsed.tables.firstOrNull { it.name == "mod" }?.nameStartOffset ?: 0
                problems += descriptor(
                    file,
                    manager,
                    anchor,
                    anchor + 1,
                    "Missing required [mod] key '$missingKey'.",
                    isOnTheFly,
                )
            }
        }

        parsed.assignments
            .filter { it.tableName == "mod" && it.key == "mod_id" }
            .forEach { assignment ->
                val modId = assignment.value.trim().trim('"')
                if (!modId.matches(Regex("""[a-z][a-z0-9_]{1,63}"""))) {
                    problems += descriptor(
                        file,
                        manager,
                        assignment.valueStartOffset,
                        assignment.valueStartOffset + assignment.value.length,
                        "mod_id must start with a lowercase letter and contain only lowercase letters, digits, and underscores.",
                        isOnTheFly,
                    )
                }
            }

        if (ProjectTomlParser.isForge1201(text)) {
            parsed.assignments
                .filter { it.value.contains("dev.latvian.mods:kubejs-forge:") && !it.value.contains(PickAidTemplateSchema.FORGE_1201_KUBEJS) }
                .forEach { assignment ->
                    problems += descriptor(
                        file,
                        manager,
                        assignment.valueStartOffset,
                        assignment.valueStartOffset + assignment.value.length,
                        "Forge 1.20.1 should use KubeJS Forge ${PickAidTemplateSchema.FORGE_1201_KUBEJS.substringAfterLast(':')}.",
                        isOnTheFly,
                        ReplaceKubeJsVersionFix(),
                    )
                }
        }

        inspectNativeLibraries(file, manager, isOnTheFly, parsed.assignments, problems)

        return problems.takeIf { it.isNotEmpty() }?.toTypedArray()
    }

    private fun inspectNativeLibraries(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean,
        assignments: List<TomlAssignment>,
        problems: MutableList<ProblemDescriptor>,
    ) {
        val basePath = file.project.basePath ?: return
        assignments
            .filter { it.tableName?.startsWith("native_libraries.") == true && it.key == "platforms" }
            .forEach { assignment ->
                val libraryName = assignment.tableName?.substringAfter("native_libraries.").orEmpty()
                ProjectTomlParser.stringArrayValues(assignment.value).forEach { platform ->
                    val platformDir = File(basePath, "native-libs/$libraryName/$platform")
                    if (!platformDir.isDirectory) {
                        problems += descriptor(
                            file,
                            manager,
                            assignment.valueStartOffset,
                            assignment.valueStartOffset + assignment.value.length,
                            "Declared native platform '$platform' is missing native-libs/$libraryName/$platform.",
                            isOnTheFly,
                            CreateNativePlatformDirectoryFix(libraryName, platform),
                        )
                    }
                }
            }
    }

    private fun descriptor(
        file: PsiFile,
        manager: InspectionManager,
        startOffset: Int,
        endOffset: Int,
        message: String,
        isOnTheFly: Boolean,
        vararg fixes: com.intellij.codeInspection.LocalQuickFix,
    ): ProblemDescriptor {
        val textLength = file.textLength
        val start = startOffset.coerceIn(0, textLength)
        val end = endOffset.coerceIn(start + 1, textLength.coerceAtLeast(start + 1))
        val element = file.findElementAt(start) ?: file
        val relativeStart = (start - element.textRange.startOffset).coerceAtLeast(0)
        val relativeEnd = (end - element.textRange.startOffset).coerceIn(relativeStart + 1, element.textLength.coerceAtLeast(relativeStart + 1))

        return manager.createProblemDescriptor(
            element,
            TextRange(relativeStart, relativeEnd),
            message,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            isOnTheFly,
            *fixes,
        )
    }
}
