# 명세

### 1) 목표

- Compose Navigation의 **route 설계**, **인자 전달**, **백스택**, **콜백 기반 네비게이션 분리**

### 2) 화면

- `MemoListScreen`
- `MemoDetailScreen(memoId)`
- `MemoEditScreen(memoId?)` (신규/수정 겸용)

### 3) 기능 요구사항 (Must)

- 목록에서 메모 선택 → 상세로 이동
- 상세에서 수정 버튼 → 편집으로 이동
- 저장 후: 편집 → 상세로 복귀(저장된 내용 반영)
- 삭제 후: 상세 → 목록으로 복귀(항목 제거 반영)
- 뒤로가기: 시스템 back/업 네비게이션 동작 정상

### 4) 데이터 모델

- `Memo(id: String, title: String, body: String, updatedAt: Long)`

### 5) 규칙 (Must)

- **복잡 객체를 네비게이션으로 넘기지 말고 ID만 전달**(SSOT에서 로드).
- 화면에 `NavController` 직접 주입 금지(테스트/재사용성 위해 **navigate 콜백**만 전달).
- 편집 화면은 “입력 임시 상태”를 `rememberSaveable`로 관리(대용량 객체 번들 저장 금지).

### 6) 완료 기준

- “ID 기반 라우팅”으로 모든 이동이 구성됨
- 네비게이션 로직은 NavHost 쪽에 모이고, Screen은 콜백만 호출
