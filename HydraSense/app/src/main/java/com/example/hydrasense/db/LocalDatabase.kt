package com.example.hydrasense.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val age: Int,
    val weight: Double,
    val height: Double,
    val gender: String,
    val dob: String,
    val dailyGoal: Double,
    val avatarRes: Int,
    val googleId: String? = null
)

@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val phValue: Double,
    val colorIndex: Int,
    val timestamp: Long,
    val isSynced: Boolean = false
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): UserEntity?
}

@Dao
interface ReadingDao {
    @Query("SELECT * FROM readings WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getReadingsForUser(email: String): Flow<List<ReadingEntity>>

    @Insert
    fun insertReading(reading: ReadingEntity)

    @Query("SELECT * FROM readings WHERE isSynced = 0")
    fun getUnsyncedReadings(): List<ReadingEntity>

    @Update
    fun updateReading(reading: ReadingEntity)
}

@Database(entities = [UserEntity::class, ReadingEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hydrasense_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
