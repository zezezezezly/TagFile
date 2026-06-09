package com.tagfile.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.enhance.ui.EnhanceScreen
import com.tagfile.app.enhance.ui.FilterLibraryScreen
import com.tagfile.app.enhance.ui.FilterSettingsScreen
import com.tagfile.app.ui.category.CategoryScreen
import com.tagfile.app.ui.filelist.FileListScreen
import com.tagfile.app.ui.home.HomeScreen
import com.tagfile.app.ui.shelf.BookDetailScreen
import com.tagfile.app.ui.shelf.BookListScreen
import com.tagfile.app.ui.shelf.ShelfScreen
import com.tagfile.app.ui.settings.ShelfSettingsScreen
import com.tagfile.app.ui.imageviewer.ImageViewerScreen
import com.tagfile.app.ui.search.SearchScreen
import com.tagfile.app.ui.settings.PersonalizationScreen
import com.tagfile.app.ui.settings.SettingsScreen
import com.tagfile.app.ui.taggedfiles.TaggedFilesScreen
import com.tagfile.app.ui.tagmanager.TagManagerScreen
import com.tagfile.app.ui.typefiles.TypeFilesScreen
import org.json.JSONArray

object Routes {
    const val HOME = "home"
    const val FILE_LIST = "file_list?dir={dir}"
    const val TAG_MANAGER = "tag_manager"
    const val TAGGED_FILES = "tagged_files/{tagId}"
    const val TYPE_FILES = "type_files/{fileType}"
    const val SEARCH = "search"
    const val CATEGORY = "category"
    const val SETTINGS = "settings"
    const val PERSONALIZATION = "personalization"
    const val IMAGE_VIEWER = "image_viewer?paths={paths}&index={index}"
    const val ENHANCE = "enhance?path={path}"
    const val FILTER_LIBRARY = "filter_library"
    const val FILTER_SETTINGS = "filter_settings/{filterId}"
    const val SHELF = "shelf"
    const val SHELF_SETTINGS = "shelf_settings"
    const val BOOK_LIST = "book_list?query={query}&mode={mode}&sortMode={sortMode}"
    const val BOOK_DETAIL = "book_detail/{bookId}"
    const val BOOK_VIEWER = "book_viewer/{bookId}"

    const val FILE_LIST_NO_ARG = "file_list"

    fun taggedFiles(tagId: Long) = "tagged_files/$tagId"
    fun typeFiles(fileType: FileType) = "type_files/${fileType.name}"
    fun fileList(dir: String? = null) = if (dir != null) "file_list?dir=${android.net.Uri.encode(dir)}"
        else "file_list"
    fun filterSettings(filterId: Long = -1L) = "filter_settings/$filterId"
    fun bookViewer(bookId: Long) = "book_viewer/$bookId"
    fun bookDetail(bookId: Long) = "book_detail/$bookId"
    fun bookList(query: String = "", mode: String = "TITLE", sortMode: String = "TITLE") =
        "book_list?query=${android.net.Uri.encode(query)}&mode=${android.net.Uri.encode(mode)}&sortMode=${android.net.Uri.encode(sortMode)}"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToFiles = {
                    navController.navigate(Routes.FILE_LIST_NO_ARG)
                },
                onNavigateToCategory = {
                    navController.navigate(Routes.CATEGORY)
                },
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToTags = {
                    navController.navigate(Routes.TAG_MANAGER)
                },
                onNavigateToFilter = {
                    navController.navigate(Routes.FILTER_LIBRARY)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToShelf = {
                    navController.navigate(Routes.SHELF)
                }
            )
        }

        composable(
            route = Routes.FILE_LIST,
            arguments = listOf(
                navArgument("dir") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            FileListScreen(
                onNavigateHome = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToImageViewer = { paths, index ->
                    navigateToImageViewer(navController, paths, index)
                }
            )
        }

        composable(Routes.TAG_MANAGER) {
            TagManagerScreen(
                onNavigateBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onNavigateToTaggedFiles = { tagId ->
                    navController.navigate(Routes.taggedFiles(tagId))
                }
            )
        }

        composable(
            route = Routes.TAGGED_FILES,
            arguments = listOf(
                navArgument("tagId") { type = NavType.LongType }
            )
        ) {
            TaggedFilesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDirectory = { dirPath ->
                    navController.navigate(Routes.fileList(dirPath))
                },
                onNavigateToImageViewer = { paths, index ->
                    navigateToImageViewer(navController, paths, index)
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onNavigateToDirectory = { dirPath ->
                    navController.navigate(Routes.fileList(dirPath))
                },
                onNavigateToTaggedFiles = { tagId ->
                    navController.navigate(Routes.taggedFiles(tagId))
                },
                onNavigateToImageViewer = { paths, index ->
                    navigateToImageViewer(navController, paths, index)
                }
            )
        }

        composable(
            route = Routes.TYPE_FILES,
            arguments = listOf(
                navArgument("fileType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileTypeName = backStackEntry.arguments?.getString("fileType") ?: FileType.IMAGE.name
            val fileType = try { FileType.valueOf(fileTypeName) } catch (_: Exception) { FileType.IMAGE }
            TypeFilesScreen(
                fileType = fileType,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToImageViewer = { paths, index ->
                    navigateToImageViewer(navController, paths, index)
                }
            )
        }

        composable(Routes.CATEGORY) {
            CategoryScreen(
                onNavigateBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onNavigateToTaggedFiles = { tagId ->
                    navController.navigate(Routes.taggedFiles(tagId))
                },
                onNavigateToTypeFiles = { fileType ->
                    navController.navigate(Routes.typeFiles(fileType))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onNavigateToPersonalization = {
                    navController.navigate(Routes.PERSONALIZATION)
                }
            )
        }

        composable(Routes.PERSONALIZATION) {
            PersonalizationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FILTER_LIBRARY) {
            FilterLibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreate = {
                    navController.navigate(Routes.filterSettings(-1L))
                },
                onNavigateToEdit = { filterId ->
                    navController.navigate(Routes.filterSettings(filterId))
                }
            )
        }

        composable(
            route = Routes.FILTER_SETTINGS,
            arguments = listOf(
                navArgument("filterId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) {
            FilterSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.IMAGE_VIEWER,
            arguments = listOf(
                navArgument("paths") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val pathsJson = backStackEntry.arguments?.getString("paths") ?: "[]"
            val index = backStackEntry.arguments?.getInt("index") ?: 0
            val paths: List<String> = try {
                val jsonArray = JSONArray(pathsJson)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } catch (_: Exception) { emptyList() }
            ImageViewerScreen(
                imagePaths = paths,
                initialIndex = index,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEnhance = { path ->
                    val encoded = Uri.encode(path)
                    navController.navigate("enhance?path=$encoded")
                }
            )
        }

        composable(
            route = Routes.ENHANCE,
            arguments = listOf(
                navArgument("path") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imagePath = Uri.decode(backStackEntry.arguments?.getString("path") ?: "")
            EnhanceScreen(
                imagePath = imagePath,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHELF) {
            ShelfScreen(
                onNavigateBack = { navController.popBackStack(Routes.HOME, inclusive = false) },
                onNavigateToBookDetail = { bookId ->
                    navController.navigate(Routes.bookDetail(bookId))
                },
                onNavigateToShelfSettings = {
                    navController.navigate(Routes.SHELF_SETTINGS)
                },
                onNavigateToBrowse = { query, mode ->
                    navController.navigate(Routes.bookList(query, mode))
                }
            )
        }

        composable(Routes.SHELF_SETTINGS) {
            ShelfSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BOOK_LIST,
            arguments = listOf(
                navArgument("query") { type = NavType.StringType; defaultValue = "" },
                navArgument("mode") { type = NavType.StringType; defaultValue = "TITLE" },
                navArgument("sortMode") { type = NavType.StringType; defaultValue = "TITLE" }
            )
        ) {
            BookListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBookDetail = { bookId ->
                    navController.navigate(Routes.bookDetail(bookId))
                }
            )
        }

        composable(
            route = Routes.BOOK_DETAIL,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) {
            BookDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRead = { bookId ->
                    navController.navigate(Routes.bookViewer(bookId))
                },
                onNavigateToAuthor = { author ->
                    navController.navigate(Routes.bookList(author, "AUTHOR", "AUTHOR"))
                }
            )
        }

        composable(
            route = Routes.BOOK_VIEWER,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) {
            val vm: com.tagfile.app.ui.bookviewer.BookViewerViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsState()

            if (uiState.images.isNotEmpty()) {
                ImageViewerScreen(
                    imagePaths = uiState.images,
                    initialIndex = 0,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEnhance = { path ->
                        val encoded = Uri.encode(path)
                        navController.navigate("enhance?path=$encoded")
                    }
                )
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(
                        text = uiState.error ?: "书籍不存在",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

fun navigateToImageViewer(
    navController: NavHostController,
    imagePaths: List<String>,
    index: Int
) {
    if (imagePaths.isEmpty()) return
    val jsonArray = JSONArray(imagePaths)
    val encodedPaths = Uri.encode(jsonArray.toString())
    val safeIndex = index.coerceIn(0, (imagePaths.size - 1).coerceAtLeast(0))
    navController.navigate("image_viewer?paths=$encodedPaths&index=$safeIndex")
}
