package com.example.memo_lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.memo_lite.presentation.navigation.MemoAppNavHost
import com.example.memo_lite.ui.theme.MemoLiteTheme

/*
* 25. MainActivity의 역할 전환 (Entry Point)
* - 개념
*       앱이 시작될 때 OS에 의해 가장 먼저 호출되는 클래스입니다.
* - 왜 싹 지우고 NavHost만 넣었을까?
*       예전 방식은 MainActivity 안에 직접 버튼이나 텍스트를 그렸지만, 지금은 **"화면 전환(Navigation)"**이 중심입니다.
*       MainActivity는 단순히 앱을 실행하는 틀만 제공하고, 실제 어떤 화면을 보여줄지는 MemoAppNavHost가 결정하도록 권한을 넘긴 것입니다.
* - 🍎 iOS 비교
*       SwiftUI 프로젝트의 App.swift 파일과 같습니다.
*       WindowGroup 안에 최상위 뷰(보통 ContentView나 NavigationStack)를 하나 딱 넣어주는 것과 완벽하게 동일한 구조입니다.
*/

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