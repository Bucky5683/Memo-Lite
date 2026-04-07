package com.example.memo_lite.presentation.navigation

// 앱의 모든 화면 주소(Route)를 정의하는 클래스입니다.
/*
* Sealed Class를 활용한 네비게이션 정의
* - 개념
*       앱에서 이동 가능한 모든 화면의 **'이름표(Route)'**를 한곳에 모아 관리하는 방식입니다.
* - 왜 썼을까?
*       화면 주소를 "memo_detail"처럼 생으로 타이핑하면 오타가 나기 쉽습니다.
*       Screen.MemoList.route처럼 클래스를 이용하면 오타 걱정 없이 자동 완성 기능을 쓸 수 있어 안전합니다.
* - 🍎 iOS 비교
*       SwiftUI에서 네비게이션 경로를 관리하기 위해 만드는 enum Route: Hashable과 완전히 똑같은 역할입니다.
* */
sealed class Screen(val route: String) {
    // 1. 목록 화면 (주소: "memo_list")
    object MemoList : Screen("memo_list")
    // 2. 상세 화면 (주소: "memo_detail/ID값")
    object MemoDetail : Screen("memo_detail/{memoId}") {
        fun createRoute(memoId: String) = "memo_detail/$memoId"
    }
    // 3. 편집 화면 (신규 작성은 주소만, 수정은 뒤에 ID를 붙임)
    object MemoEdit : Screen("memo_edit?memoId={memoId}") {
        fun createRoute(memoId: String? = null) =
            if (memoId != null) "memo_edit?memoId=$memoId" else "memo_edit"
    }
}