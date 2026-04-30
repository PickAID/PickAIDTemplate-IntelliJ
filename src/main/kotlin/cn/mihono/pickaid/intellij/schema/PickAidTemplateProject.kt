package cn.mihono.pickaid.intellij.schema

import com.intellij.psi.PsiFile

object PickAidTemplateProject {
    fun isProjectToml(file: PsiFile): Boolean {
        if (!ProjectTomlParser.isPickAidTomlName(file.name)) {
            return false
        }

        val text = file.text
        if (text.contains("template_version") || text.contains("[native_libraries.") || text.contains("[dev_packs]")) {
            return true
        }

        val directory = file.virtualFile?.parent ?: return false
        if (directory.findFileByRelativePath("gradle/template-defaults.toml") != null) {
            return true
        }

        if (directory.findFileByRelativePath("src/templates") != null) {
            return true
        }

        if (file.name == "project.local.toml" && directory.findChild("project.toml") != null) {
            return true
        }

        return false
    }
}
