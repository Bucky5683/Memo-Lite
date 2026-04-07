package com.example.memo_lite.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memo_lite.data.repository.FakeMemoRepository
import com.example.memo_lite.domain.model.Memo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1) 목록 뷰모델
class MemoListViewModel: ViewModel() {
    private val repository = FakeMemoRepository

    // DB의 변경사항을 실시간으로 구독(SSOT)
    /*
    * 가짜 DB에서 메모 목록이 흘러나오는 차가운 파이프(Flow)를 연결
    * - stateIn : 이 파이프를 화면이 바로 읽을 수 있는 stateFlow로 변경
    * - SharingStarted.WhileSubscribed(5000) :
    *       화면을 회전하거나 잠깐 홈 화면에 다녀올 때, 안드로이드는 화면을 부수고 다시 만듦
    *       -> 이때마다 DB 연결을 끊었다가 다시 맺으면 비효율적일 것임
    *       그래서 "화면이 사라져도 딱 5초만 더 구독을 유지하면서 기다리셈!"하고 유예기간을 준 것 (최적화 기술)
    * - emptyList() : DB에서 데이터를 다 불러오기 전, 아주 찰나의 순간 동안 화면에 띄워둘 초기값
    * */
    val memos: StateFlow<List<Memo>> = repository.getAllMemos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

// 2) 상세 뷰모델 (Backing Property)
class MemoDetailViewModel: ViewModel() {
    private val repository = FakeMemoRepository
    /*
     * - '_memo'와 'memo'를 나누는 이유: UI(화면)쪽에서 마음대로 메모 내용을 수정해 버리면 데이터가 꼬임
     *      => 그래서 뷰모델 내부에서 _memo를, 외부에서 asStateFlow()로 memo로 변환해서 사용 (캡슐화)
     * - loadMemo(id) :
     *      목록에서 항목을 클릭해 상세화면으로 넘어왔을 때, 넘겨받은 id를 가지고 DB에서 최신데이터를 다시 불러옴 (SSOT 원칙)
     *      목록 화면이 들고 있던 데이터를 재활용하지 않고 무조건 DB(진실 공급원)을 다시 확인하는 것이 핵심
     *  - onSuccess: () -> Unit 콜백 :
     *      삭제 버튼을 누르면 비동기(백그라운드)로 삭제 작업이 돔.
     *      삭제가 언제 끝날지 화면은 모르기 떄문에 뷰모델이 삭제를 끝내고 나서 onSuccess()를 호출해주면,
     *      화면은 그제서야 끝난 것을 확인하고 뒤로가야지(popBackStack)하고 움직이게 됨
     */
    private val _memo = MutableStateFlow<Memo?>(null)
    val memo = _memo.asStateFlow()

    fun loadMemo(id: String) {
        viewModelScope.launch {
            _memo.value = repository.getMemoById(id)
        }
    }

    fun deleteMemo(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMemo(id)
            onSuccess() // 삭제 완료 후 람다 콜백 생성
        }
    }
}

// 3) 편집 뷰모델
class MemoEditViewModel: ViewModel() {
    /*
    * - 물탱크가 없는 이유(stateFlow가 없는 이유) :
    *       명세에 편집화면은 '입력 임시 상태'를 rememberSavable로 관리하라 라는 규칙이 있음
    *       사용자가 타자를 치는 그 순간의 글자들은 뷰모델이 아니라 화면(UI) 쪽에서 임시로 들고 있음
    * - getInitialMemo(id) :
    *       기존 메모를 수정할 때, 화면이 "뷰모델아, 나 빈칸 채워야하니까 기존 데이터 딱 한번만 주셈"할 때 쓰임.
    *       지속해서 관찰(flow)할 필요가 없으니 그냥 suspend 함수로 값만 딱 뱉고 끝남
    * - id ?: "" (멜비스 연산자)
    *       id가 만약에 null(신규작성)이라면 빈 문자열("")을 대신 집어넣으라는 뜻
    *       빈문자열이 Repository로 넘어가면 가짜 DB가 이를 감지하고 새로운 UUID를 생성하여 저장
    */
    private val repository = FakeMemoRepository

    // 수정 시 기존 데이터를 불러오기 위함
    suspend fun getInitialMemo(id: String): Memo? {
        return repository.getMemoById(id)
    }

    fun save(id: String?, title: String, body: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val memo = Memo(
                id = id ?: "",  // null 이면 빈 문자열 (Repository에서 새 ID 발급)
                title = title,
                body = body
            )
            repository.saveMemo(memo)
            onSuccess()
        }
    }
}