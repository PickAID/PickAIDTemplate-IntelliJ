package cn.mihono.pickaid.intellij.quickfix

import cn.mihono.pickaid.intellij.schema.PickAidTemplateSchema
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

class ReplaceKubeJsVersionFix : LocalQuickFix {
    override fun getFamilyName(): String = "Use the PickAID Forge 1.20.1 KubeJS coordinate"

    override fun getName(): String = "Replace with ${PickAidTemplateSchema.FORGE_1201_KUBEJS}"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement ?: return
        val file = element.containingFile ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        val lineNumber = document.getLineNumber(element.textRange.startOffset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val oldLine = document.getText(com.intellij.openapi.util.TextRange(lineStart, lineEnd))
        val newLine = oldLine.replace(
            Regex("""dev\.latvian\.mods:kubejs-forge:[^"\s}]+"""),
            PickAidTemplateSchema.FORGE_1201_KUBEJS,
        )

        if (newLine == oldLine) {
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(lineStart, lineEnd, newLine)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
