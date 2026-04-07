package com.example.memo_lite.presentation.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memo_lite.domain.model.Memo
import com.example.memo_lite.presentation.ui.MemoDetailScreen
import com.example.memo_lite.presentation.ui.MemoEditScreen
import com.example.memo_lite.presentation.ui.MemoListScreen
import com.example.memo_lite.presentation.viewModel.MemoDetailViewModel
import com.example.memo_lite.presentation.viewModel.MemoEditViewModel
import com.example.memo_lite.presentation.viewModel.MemoListViewModel

@Composable
fun MemoAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MemoList.route
    ) {
        // [목록 화면]
        composable(Screen.MemoList.route) {
            val viewModel: MemoListViewModel = viewModel()
            val memos by viewModel.memos.collectAsState()

            MemoListScreen(
                memos = memos,
                onMemoClick = { memoId ->
                    navController.navigate(Screen.MemoDetail.createRoute(memoId))
                },
                onAddMemoClick = {
                    navController.navigate(Screen.MemoEdit.createRoute(null))
                }
            )
        }

        // [상세 화면]
        composable(
            route = Screen.MemoDetail.route,
            arguments = listOf(navArgument("memoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getString("memoId") ?: return@composable
            val viewModel: MemoDetailViewModel = viewModel()

            // 진입 시 ID를 기반으로 데이터 로드 (SSOT)
            LaunchedEffect(memoId) {
                viewModel.loadMemo(memoId)
            }
            val memo by viewModel.memo.collectAsState()

            MemoDetailScreen(
                memo = memo,
                onEditClick = { id ->
                    navController.navigate(Screen.MemoEdit.createRoute(id))
                },
                onDeleteClick = { id ->
                    viewModel.deleteMemo(id) {
                        navController.popBackStack() // 삭제 성공 시 목록으로 복귀
                    }
                }
            )
        }

        // [편집 화면]
        composable(
            route = Screen.MemoEdit.route,
            arguments = listOf(
                navArgument("memoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getString("memoId")
            val viewModel: MemoEditViewModel = viewModel()
            var initialMemo by remember { mutableStateOf<Memo?>(null) }

            // 수정 모드일 때 기존 데이터 1회 로드
            LaunchedEffect(memoId) {
                if (memoId != null) {
                    initialMemo = viewModel.getInitialMemo(memoId)
                }
            }

            // initialMemo가 세팅되거나 신규 작성(memoId==null)일 때만 화면 렌더링
            if (memoId == null || initialMemo != null) {
                MemoEditScreen(
                    initialMemo = initialMemo,
                    onSaveClick = { title, body ->
                        viewModel.save(id = memoId, title = title, body = body) {
                            navController.popBackStack() // 저장 성공 시 이전 화면으로 복귀
                        }
                    }
                )
            }
        }
    }
}