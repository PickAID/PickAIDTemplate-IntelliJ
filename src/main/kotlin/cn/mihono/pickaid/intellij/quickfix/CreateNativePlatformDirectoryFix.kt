package cn.mihono.pickaid.intellij.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager

class CreateNativePlatformDirectoryFix(
    private val libraryName: String,
    private val platformName: String,
) : LocalQuickFix {
    override fun getFamilyName(): String = "Create native platform directory"

    override fun getName(): String = "Create native-libs/$libraryName/$platformName"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val basePath = project.basePath ?: return
        VfsUtil.createDirectories("$basePath/native-libs/$libraryName/$platformName")
        VirtualFileManager.getInstance().syncRefresh()
    }
}
