package com.example.memo_lite.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.memo_lite.domain.model.Memo
import androidx.compose.ui.tooling.preview.Preview

/*
* [MemoListScreen: 메모 목록 화면]
* - Scaffold (화면 뼈대):
*       머티리얼 디자인의 기본 뼈대를 잡아줍니다.
*       여기서는 우측 하단에 동그랗게 떠 있는 FloatingActionButton (추가 버튼)을 아주 쉽게 배치하기 위해 사용했습니다.
*       🍎 iOS 비교: 화면 전체를 덮는 NavigationView나, ZStack으로 우측 하단에 버튼을 직접 띄우는 것과 유사합니다.
* - LazyColumn & items(memos) (무한 스크롤 리스트):
*       뷰모델에서 넘겨받은 memos 리스트를 하나씩 꺼내 화면에 그려줍니다.
*       🍎 iOS 비교: List 안에 ForEach(memos) { memo in ... }를 돌리는 것과 완벽하게 똑같습니다.
* - Card & clickable (개별 항목):
*       메모의 제목을 예쁜 그림자 진 네모 상자(Card)에 담습니다.
*       그리고 .clickable을 달아 사용자가 터치하면 onMemoClick(memo.id) 신호를 위로 쏘아 올려 상세 화면으로 이동하게 만듭니다.
*/
@Composable
fun MemoListScreen(
    memos: List<Memo>,
    onMemoClick: (String) -> Unit,
    onAddMemoClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMemoClick) { Text("+") }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(memos) { memo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onMemoClick(memo.id) }
                ) {
                    Text(text = memo.title, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

/*
* [MemoDetailScreen: 메모 상세 화면]
* - if (memo == null) return (방어 코드):
*       뷰모델이 DB에서 데이터를 가져오는 찰나의 순간에 데이터가 없어 앱이 튕기는(Crash) 현상을 막아주는 중요한 안전장치입니다.
* - Column & Row (배치):
*       제목과 내용은 위아래로 쌓아야 하니 Column에 넣고, 수정/삭제 버튼은 양옆으로 나란히 둬야 하니 Row에 넣었습니다.
*       🍎 iOS 비교: Column은 VStack, Row는 HStack과 완벽하게 동일합니다.
* - Spacer (빈 공간):
*       컴포넌트들이 너무 다닥다닥 붙지 않게 투명한 여백 상자를 끼워 넣는 용도입니다.
*       🍎 iOS 비교: Spacer() 와 완전히 동일한 역할입니다.
* - style = MaterialTheme.typography... (스타일링):
*       글씨 크기나 굵기를 일일이 지정하지 않고, 구글이 미리 예쁘게 세팅해 둔 '제목 스타일', '본문 스타일'을 가져다 쓴 것입니다.
*/
@Composable
fun MemoDetailScreen(
    memo: Memo?,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    if (memo == null) return // 로딩 중이거나 없는 메모

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = memo.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = memo.body, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = { onEditClick(memo.id) }) { Text("수정") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onDeleteClick(memo.id) }) { Text("삭제") }
        }
    }
}

/*
* [MemoEditScreen: 메모 편집 화면]
* - var title by rememberSaveable { ... } (임시 상태 관리 ⭐️):
*       화면(UI) 자체가 임시로 글씨를 기억하도록 만들며, 화면 회전이나 잠깐 앱을 내렸을 때도 쓰던 글이 절대 날아가지 않게 지켜줍니다.
*       🍎 iOS 비교: 뷰 내부에 선언하는 @State private var title: String = "" 와 완전히 동일한 역할입니다.
* - OutlinedTextField (입력창):
*       value = title로 현재 기억하는 값을 보여주고, onValueChange로 타자를 칠 때마다 쳐진 글자를 다시 title 변수에 덮어씌워 화면을 갱신합니다.
*       🍎 iOS 비교: TextField(text: $title)처럼 양방향 바인딩을 코틀린 방식으로 구현한 것입니다.
* - onSaveClick(title, body) (저장 신호 보내기):
*       "저장" 버튼을 누르면, 지금까지 임시로 잘 기억해 둔 title과 body를 묶어서 상위로 휙 던져버립니다.
*       뷰모델에 저장하고 화면을 뒤로 빼는 역할은 상위(NavHost)에서 모두 알아서 처리합니다.
*/
@Composable
fun MemoEditScreen(
    initialMemo: Memo?,
    onSaveClick: (title: String, body: String) -> Unit
) {
    // 규칙 5 적용: 프로세스 종료 시에도 입력 상태 유지 (대용량 객체 X, 텍스트만)
    var title by rememberSaveable { mutableStateOf(initialMemo?.title ?: "") }
    var body by rememberSaveable { mutableStateOf(initialMemo?.body ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("내용") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSaveClick(title, body) }) {
            Text("저장")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoListScreenPreview() {
    val fakeMemos = listOf(
        Memo(id = "1", title = "오늘의 할 일", body = "안드로이드 공부하기"),
        Memo(id = "2", title = "장보기 목록", body = "우유, 달걀, 빵")
    )
    MemoListScreen(
        memos = fakeMemos,
        onMemoClick = {}, // 미리보기에서는 동작이 필요 없으므로 비워둡니다.
        onAddMemoClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MemoDetailScreenPreview() {
    val fakeMemo = Memo(id = "1", title = "상세 보기 테스트", body = "여기에 본문 내용이 나옵니다.")
    MemoDetailScreen(
        memo = fakeMemo,
        onEditClick = {},
        onDeleteClick = {}
    )
}

@Preview(showBackground = true, name = "신규 작성")
@Composable
fun MemoEditScreenNewPreview() {
    MemoEditScreen(
        initialMemo = null, // 신규 작성이므로 데이터 없음
        onSaveClick = { _, _ -> }
    )
}

@Preview(showBackground = true, name = "수정 모드")
@Composable
fun MemoEditScreenEditPreview() {
    val fakeMemo = Memo(id = "1", title = "기존 제목", body = "기존 내용")
    MemoEditScreen(
        initialMemo = fakeMemo, // 기존 데이터가 채워진 상태
        onSaveClick = { _, _ -> }
    )
}