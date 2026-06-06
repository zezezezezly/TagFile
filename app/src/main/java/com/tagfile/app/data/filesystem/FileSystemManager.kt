package com.tagfile.app.data.filesystem

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import com.tagfile.app.domain.model.FileItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSystemManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun getFiles(directoryPath: String): List<FileItem> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()?.map { file ->
            file.toFileItem()
        }?.sortedWith(compareBy<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()
    }

    fun getFileInfo(filePath: String): FileItem? {
        val file = File(filePath)
        return if (file.exists()) file.toFileItem() else null
    }

    fun copyFile(sourcePath: String, destinationDir: String): String {
        val source = File(sourcePath)
        val destDir = File(destinationDir)
        if (!destDir.exists()) destDir.mkdirs()

        var destFile = File(destDir, source.name)
        var counter = 1
        while (destFile.exists()) {
            val nameWithoutExt = source.nameWithoutExtension
            val ext = source.extension
            destFile = File(destDir, "$nameWithoutExt ($counter).$ext")
            counter++
        }

        if (source.isDirectory) {
            source.copyRecursively(destFile, overwrite = false)
        } else {
            source.copyTo(destFile, overwrite = false)
        }
        return destFile.absolutePath
    }

    fun moveFile(sourcePath: String, destinationDir: String): String {
        val source = File(sourcePath)
        val destDir = File(destinationDir)
        if (!destDir.exists()) destDir.mkdirs()

        val destFile = File(destDir, source.name)
        if (destFile.exists()) {
            throw IllegalStateException("目标文件已存在: ${destFile.absolutePath}")
        }

        source.renameTo(destFile)
        return destFile.absolutePath
    }

    @Suppress("unused")
    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.deleteRecursively()
        }
    }

    fun moveToTrash(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) return false

        val trashDir = File(context.filesDir, ".trash")
        if (!trashDir.exists()) trashDir.mkdirs()

        val trashFile = File(trashDir, "${System.currentTimeMillis()}_${file.name}")
        return file.renameTo(trashFile)
    }

    fun renameFile(filePath: String, newName: String): String {
        val file = File(filePath)
        val parentDir = file.parentFile ?: throw IllegalStateException("无法获取父目录")
        val newFile = File(parentDir, newName)

        if (newFile.exists()) {
            throw IllegalStateException("文件已存在: $newName")
        }

        file.renameTo(newFile)
        return newFile.absolutePath
    }

    fun createDirectory(parentPath: String, name: String): String {
        val dir = File(parentPath, name)
        if (dir.exists()) {
            throw IllegalStateException("目录已存在: $name")
        }
        dir.mkdirs()
        return dir.absolutePath
    }

    fun getStorageRoots(): List<String> {
        val roots = mutableListOf<String>()
        Environment.getExternalStorageDirectory()?.let { roots.add(it.absolutePath) }
        context.getExternalFilesDirs(null).forEach { file ->
            file?.let { f ->
                var path = f.absolutePath
                val index = path.indexOf("/Android/data/")
                if (index > 0) {
                    path = path.take(index)
                    if (path !in roots) roots.add(path)
                }
            }
        }
        return roots
    }

    @Suppress("unused")
    fun getCommonDirectories(): List<Pair<String, String>> {
        return listOf(
            "内部存储" to (Environment.getExternalStorageDirectory()?.absolutePath ?: ""),
            "下载" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            "图片" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
            "文档" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
            "音乐" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath,
            "视频" to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).absolutePath
        ).filter { File(it.second).exists() }
    }

    fun getMimeType(filePath: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    @Suppress("unused")
    fun walkFiles(directoryPath: String): Sequence<File> {
        return File(directoryPath).walkTopDown()
    }

    private fun File.toFileItem(): FileItem {
        return FileItem(
            path = absolutePath,
            name = name,
            isDirectory = isDirectory,
            size = if (isDirectory) 0L else length(),
            lastModified = lastModified(),
            extension = extension,
            mimeType = if (isFile) getMimeType(absolutePath) else null
        )
    }
}
