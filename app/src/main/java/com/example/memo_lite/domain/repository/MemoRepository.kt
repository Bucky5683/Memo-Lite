package com.example.memo_lite.domain.repository

import com.example.memo_lite.domain.model.Memo
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    fun getAllMemos(): Flow<List<Memo>>
    suspend fun getMemoById(id: String): Memo?
    suspend fun saveMemo(memo: Memo)
    suspend fun deleteMemo(id: String)
}