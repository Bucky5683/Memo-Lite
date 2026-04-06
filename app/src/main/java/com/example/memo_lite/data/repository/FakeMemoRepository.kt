package com.example.memo_lite.data.repository

import com.example.memo_lite.domain.model.Memo
import com.example.memo_lite.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

// 초보자 테스트용 싱글톤 객체 (실무에서는 Hilt 같은 DI 도구로 주입받음)
object FakeMemoRepository : MemoRepository {
    /*
        코틀린에서 class 대신 object라는 키워드를 사용하면, 앱이 실행될 때 메모리에 이 객체를 딱 하나만 만듭니다. 이를 '싱글톤(Singleton) 패턴'이라고 합니다.
        - 이유: 데이터베이스가 여러 개 생기면 데이터가 꼬이겠죠? 그래서 앱의 모든 화면(목록, 상세, 편집)이 이 똑같은 하나의 저장소를 공유해서 바라보게 만든 것입니다.
        - 🍎iOS 비교: Swift에서 static let shared = FakeMemoRepository() 형태로 싱글톤을 만드는 것과 똑같은 목적입니다.
    */

    /*
    * 메모들의 목록(List<Memo>)을 담고 있으며, 앱을 처음 켰을 때는 메모가 하나도 없으니 emptyList()(빈 리스트)로 텅 빈 상태를 만들어 두었습니다.
    */
    private val memoListFlow = MutableStateFlow<List<Memo>>(emptyList())

    /*
    * 저장소 밖(ViewModel 등)에서 데이터를 달라고 할 때, 물탱크(memoListFlow)를 그대로 연결해 줍니다.
    * - 이 함수를 통해 ViewModel은 파이프(Flow)를 구독
    * - 앞으로 메모가 추가되거나 지워질 때마다 알아서 최신 리스트를 전달받아 UI를 갱신하게 됩니다.
    */
    override fun getAllMemos(): Flow<List<Memo>> = memoListFlow

    /*
    * 상세 화면에 들어갈 때, 특정 ID의 메모 내용만 쏙 뽑아오는 함수입니다.
    * - find 함수를 써서, 물탱크 안의 메모들을 쭉 뒤지기
    * - 내가 넘겨준 id와 똑같은 메모가 있으면 반환하고, 만약 못 찾으면 null(없음)을 반환합니다.
    */
    override suspend fun getMemoById(id: String): Memo? {
        return memoListFlow.value.find { it.id == id }
    }

    override suspend fun saveMemo(memo: Memo) {
        // 1. 기존 물탱크의 물(리스트)을 바가지에 새로 퍼담습니다 (수정 가능한 리스트로 복사)
        val currentList = memoListFlow.value.toMutableList()

        // 2. 내가 저장하려는 메모의 ID가 기존 바가지(리스트)에 몇 번째 칸에 있는지 찾습니다.
        val index = currentList.indexOfFirst { it.id == memo.id }

        if (index != -1) {
            // 3-1. 기존 메모 수정 모드
            // indexOfFirst는 못 찾으면 -1을 줍니다. -1이 아니라는 건 "이미 존재한다"는 뜻입니다.
            // 기존 자리에 새 메모 내용을 그대로 덮어씌웁니다.
            currentList[index] = memo
        } else {
            // 3-2. 신규 메모 추가 모드
            // 리스트에 없으므로 새 메모입니다. ID가 아예 비어있다면 UUID를 써서 새 ID를 찍어줍니다.
            val newMemo = if (memo.id.isEmpty()) memo.copy(id = UUID.randomUUID().toString()) else memo
            currentList.add(newMemo) // 바가지 맨 끝에 새 메모를 추가합니다.
        }

        // 4. 물탱크 업데이트! (제일 중요)
        // 작업이 끝난 바가지(currentList)를 통째로 물탱크에 붓습니다.
        // 이 순간, 물탱크의 값이 변했으므로 Flow 파이프를 타고 화면이 쫙 갱신됩니다.
        memoListFlow.value = currentList

        /*
        * Q: 왜 굳이 리스트를 복사(toMutableList)할까요?
        * A: MutableStateFlow는 자기가 들고 있는 객체가 "완전히 새로운 객체"로 교체되어야만 '값이 변했다!'라고 인식하고 파이프에 물을 흘려보냅니다.
        * 기존 리스트 안의 내용물만 슬쩍 바꾸면 변화를 눈치채지 못하기 때문에, 통째로 복사해서 작업한 뒤에 새 리스트를 통으로 꽂아주는 것입니다.
        */
    }

    override suspend fun deleteMemo(id: String) {
        val currentList = memoListFlow.value.toMutableList()

        // 해당 id를 가진 메모를 리스트에서 쏙 빼서 없애버립니다.
        currentList.removeAll { it.id == id }

        // 역시나 작업이 끝난 리스트를 다시 물탱크에 꽂아주어 UI를 갱신합니다.
        memoListFlow.value = currentList
    }
}