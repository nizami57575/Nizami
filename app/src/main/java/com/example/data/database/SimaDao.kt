package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SimaDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanById(id: Int): ScanHistoryEntity?

    @Query("SELECT * FROM social_accounts WHERE scanId = :scanId ORDER BY matchScore DESC")
    fun getSocialAccountsForScan(scanId: Int): Flow<List<SocialAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSocialAccounts(accounts: List<SocialAccountEntity>)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanById(id: Int)
}
