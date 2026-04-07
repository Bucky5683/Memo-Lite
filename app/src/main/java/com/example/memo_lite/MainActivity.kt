package com.example.memo_lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.memo_lite.presentation.navigation.MemoAppNavHost
import com.example.memo_lite.ui.theme.MemoLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. 우리가 만든 테마로 감싸기 (글꼴, 색상 등)
            MemoLiteTheme {
                // 2. 전체 화면 배경색 설정
                Surface(color = MaterialTheme.colorScheme.background) {
                    // 3. 드디어 주인공! 우리가 만든 내비게이션 관제탑 소환
                    MemoAppNavHost()
                }
            }
        }
    }
}